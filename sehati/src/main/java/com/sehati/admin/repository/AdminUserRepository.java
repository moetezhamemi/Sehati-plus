package com.sehati.admin.repository;

import com.sehati.admin.dto.UserAdminProjection;
import com.sehati.auth.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminUserRepository extends JpaRepository<User, Long> {

    @Query(value = "SELECT u.id as id, " +
                   "u.email as email, " +
                   "u.status as status, " +
                   "(SELECT r.name FROM roles r JOIN user_role ur ON r.id = ur.role_id WHERE ur.user_id = u.id LIMIT 1) as role, " +
                   "COALESCE(m.nom, p.nom, s.nom, l.nom_labo) as nom, " +
                   "COALESCE(m.prenom, p.prenom, s.prenom, l.responsable) as prenom, " +
                   "COALESCE(m.photo_profil_url, p.photo_profil_url, l.photo_profil_url) as photoUrl " +
                   "FROM `user` u " +
                   "LEFT JOIN medecins m ON u.id = m.user_id " +
                   "LEFT JOIN patients p ON u.id = p.user_id " +
                   "LEFT JOIN secretaires s ON u.id = s.user_id " +
                   "LEFT JOIN laboratoires l ON u.id = l.user_id " +
                   "LEFT JOIN specialites sp ON m.specialite_id = sp.id " +
                   "WHERE u.status != 'PENDING' " +
                   "AND 'ADMIN' != (SELECT r.name FROM roles r JOIN user_role ur ON r.id = ur.role_id WHERE ur.user_id = u.id LIMIT 1) " +
                   "AND (:search IS NULL OR LOWER(COALESCE(m.nom, p.nom, s.nom, l.nom_labo, '')) LIKE LOWER(CONCAT('%', :search, '%')) " +
                   "    OR LOWER(COALESCE(m.prenom, p.prenom, s.prenom, l.responsable, '')) LIKE LOWER(CONCAT('%', :search, '%'))) " +
                   "AND (:role IS NULL OR (SELECT r.name FROM roles r JOIN user_role ur ON r.id = ur.role_id WHERE ur.user_id = u.id LIMIT 1) = :role) " +
                   "AND (:status IS NULL OR u.status = :status) " +
                   "AND (:specialite IS NULL OR sp.nom = :specialite)",
           countQuery = "SELECT count(u.id) " +
                   "FROM `user` u " +
                   "LEFT JOIN medecins m ON u.id = m.user_id " +
                   "LEFT JOIN patients p ON u.id = p.user_id " +
                   "LEFT JOIN secretaires s ON u.id = s.user_id " +
                   "LEFT JOIN laboratoires l ON u.id = l.user_id " +
                   "LEFT JOIN specialites sp ON m.specialite_id = sp.id " +
                   "WHERE u.status != 'PENDING' " +
                   "AND 'ADMIN' != (SELECT r.name FROM roles r JOIN user_role ur ON r.id = ur.role_id WHERE ur.user_id = u.id LIMIT 1) " +
                   "AND (:search IS NULL OR LOWER(COALESCE(m.nom, p.nom, s.nom, l.nom_labo, '')) LIKE LOWER(CONCAT('%', :search, '%')) " +
                   "    OR LOWER(COALESCE(m.prenom, p.prenom, s.prenom, l.responsable, '')) LIKE LOWER(CONCAT('%', :search, '%'))) " +
                   "AND (:role IS NULL OR (SELECT r.name FROM roles r JOIN user_role ur ON r.id = ur.role_id WHERE ur.user_id = u.id LIMIT 1) = :role) " +
                   "AND (:status IS NULL OR u.status = :status) " +
                   "AND (:specialite IS NULL OR sp.nom = :specialite)",
           nativeQuery = true)
    Page<UserAdminProjection> findAllUsersWithFilters(
            @Param("search") String search,
            @Param("role") String role,
            @Param("status") String status,
            @Param("specialite") String specialite,
            Pageable pageable);

    @Query(value = "SELECT u.id as id, " +
                   "u.email as email, " +
                   "u.status as status, " +
                   "(SELECT r.name FROM roles r JOIN user_role ur ON r.id = ur.role_id WHERE ur.user_id = u.id LIMIT 1) as role, " +
                   "COALESCE(m.nom, p.nom, s.nom, l.nom_labo) as nom, " +
                   "COALESCE(m.prenom, p.prenom, s.prenom, l.responsable) as prenom, " +
                   "COALESCE(m.photo_profil_url, p.photo_profil_url, l.photo_profil_url) as photoUrl " +
                   "FROM `user` u " +
                   "LEFT JOIN medecins m ON u.id = m.user_id " +
                   "LEFT JOIN patients p ON u.id = p.user_id " +
                   "LEFT JOIN secretaires s ON u.id = s.user_id " +
                   "LEFT JOIN laboratoires l ON u.id = l.user_id " +
                   "LEFT JOIN specialites sp ON m.specialite_id = sp.id " +
                   "WHERE u.status = 'PENDING' " +
                   "AND (:search IS NULL OR LOWER(COALESCE(m.nom, p.nom, s.nom, l.nom_labo, '')) LIKE LOWER(CONCAT('%', :search, '%')) " +
                   "    OR LOWER(COALESCE(m.prenom, p.prenom, s.prenom, l.responsable, '')) LIKE LOWER(CONCAT('%', :search, '%'))) " +
                   "AND (:role IS NULL OR (SELECT r.name FROM roles r JOIN user_role ur ON r.id = ur.role_id WHERE ur.user_id = u.id LIMIT 1) = :role) " +
                   "AND (:specialite IS NULL OR sp.nom = :specialite)",
           countQuery = "SELECT count(u.id) " +
                   "FROM `user` u " +
                   "LEFT JOIN medecins m ON u.id = m.user_id " +
                   "LEFT JOIN specialites sp ON m.specialite_id = sp.id " +
                   "WHERE u.status = 'PENDING' " +
                   "AND (:search IS NULL OR LOWER(COALESCE(m.nom, '')) LIKE LOWER(CONCAT('%', :search, '%'))) " +
                   "AND (:role IS NULL OR (SELECT r.name FROM roles r JOIN user_role ur ON r.id = ur.role_id WHERE ur.user_id = u.id LIMIT 1) = :role) " +
                   "AND (:specialite IS NULL OR sp.nom = :specialite)",
           nativeQuery = true)
    Page<UserAdminProjection> findAllPendingUsersWithFilters(
            @Param("search") String search,
            @Param("role") String role,
            @Param("specialite") String specialite,
            Pageable pageable);
}
