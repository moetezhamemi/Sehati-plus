package com.sehati.common.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @SuppressWarnings("unchecked")
    public String uploadFile(MultipartFile file) {
        try {
            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'upload du document vers Cloudinary", e);
        }
    }

    @SuppressWarnings("unchecked")
    public String uploadFile(byte[] fileBytes, String filename) {
        try {
            Map<String, Object> params = ObjectUtils.asMap(
                    "resource_type", "auto",
                    "folder", "sehati-ordonnances",
                    "public_id", filename
            );
            Map<String, Object> uploadResult = cloudinary.uploader().upload(fileBytes, params);
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'upload du document brut vers Cloudinary", e);
        }
    }

    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) return;
        try {
            // URL Cloudinary standard: .../upload/v1234567/folder/filename.ext
            // On cherche à extraire "folder/filename" ou "filename"
            String[] parts = fileUrl.split("/");
            String fileWithExt = parts[parts.length - 1];
            String folder = parts[parts.length - 2];
            
            String publicId = fileWithExt.contains(".") 
                ? fileWithExt.substring(0, fileWithExt.lastIndexOf('.')) 
                : fileWithExt;
                
            // Si le dossier n'est pas un numéro de version ou "upload", on l'inclut
            if (!folder.equals("upload") && !folder.startsWith("v") && folder.matches(".*[a-zA-Z]+.*")) {
                publicId = folder + "/" + publicId;
            }
            
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            System.err.println("Erreur lors de la suppression sur Cloudinary: " + e.getMessage());
        }
    }

    /**
     * Upload a signature image as PNG (canvas is already transparent).
     * No background removal is needed.
     */
    @SuppressWarnings("unchecked")
    public String uploadSignaturePng(MultipartFile file) {
        try {
            Map<String, Object> params = ObjectUtils.asMap(
                    "folder", "sehati-signatures",
                    "format", "png",
                    "resource_type", "image"
            );
            Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), params);
            return result.get("secure_url").toString();
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'upload de la signature", e);
        }
    }

    /**
     * Upload a cachet (stamp) image to Cloudinary and apply AI background removal.
     * Returns a delivery URL with e_background_removal and f_png transformations.
     */
    @SuppressWarnings("unchecked")
    public String uploadCachetWithBgRemoval(MultipartFile file) {
        try {
            Map<String, Object> params = ObjectUtils.asMap(
                    "folder", "sehati-cachets",
                    "resource_type", "image"
            );
            Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), params);
            String publicId = result.get("public_id").toString();

            // Build delivery URL with background removal + force PNG
            String transformedUrl = cloudinary.url()
                    .transformation(new Transformation().effect("background_removal").chain().fetchFormat("png"))
                    .publicId(publicId)
                    .generate();

            return transformedUrl;
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'upload du cachet", e);
        }
    }
}
