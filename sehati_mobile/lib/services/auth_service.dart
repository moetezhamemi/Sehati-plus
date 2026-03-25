import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:google_sign_in/google_sign_in.dart';
import 'package:http/http.dart' as http;
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import '../config/api_config.dart';

class AuthService {
  static const _storage = FlutterSecureStorage();
  static const _tokenKey = 'auth_token';
  static const _roleKey = 'user_role';

  // ⚠️ CRITIQUE : serverClientId DOIT être le "Web Client ID" du backend.
  // Si idToken est null, c'est que ce Client ID n'est pas bon.
  final GoogleSignIn _googleSignIn = GoogleSignIn(
    serverClientId: '410643837425-moltq75eoklghmcmevv1enkn5gesbt0b.apps.googleusercontent.com', 
    scopes: ['email', 'profile'],
  );

  /// Connexion classique avec Email/Mot de passe
  Future<Map<String, dynamic>> login(String email, String password) async {
    try {
      final response = await http.post(
        Uri.parse('${ApiConfig.authUrl}/signin'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({'email': email, 'password': password}),
      );

      return _handleAuthResponse(response);
    } catch (e) {
      debugPrint('Erreur login email: $e');
      return {'success': false, 'message': 'Connexion impossible ($e)'};
    }
  }

  /// Inscription Patient
  Future<Map<String, dynamic>> registerPatient(
      String prenom, String nom, String email, String telephone, String password, String confirmPassword, String dateNaissance) async {
    try {
      // Nettoyer le téléphone pour ne garder que les chiffres (backend attend ^\d{8}$)
      final cleanPhone = telephone.replaceAll(RegExp(r'\D'), '');
      
      final response = await http.post(
        Uri.parse('${ApiConfig.authUrl}/signup/patient'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({
          'nom': nom.trim(),
          'prenom': prenom.trim(),
          'email': email.trim(),
          'telephone': cleanPhone,
          'dateNaissance': dateNaissance,
          'password': password,
          'confirmPassword': confirmPassword,
        }),
      );

      if (response.statusCode == 200 || response.statusCode == 201) {
        final data = jsonDecode(response.body);
        return {'success': true, 'message': data['message'] ?? 'Inscription réussie'};
      } else {
        final error = jsonDecode(response.body);
        return {'success': false, 'message': error['message'] ?? 'Erreur lors de l\'inscription'};
      }
    } catch (e) {
      debugPrint('Erreur inscription: $e');
      return {'success': false, 'message': 'Connexion impossible ($e)'};
    }
  }

  /// Connexion avec Google
  Future<Map<String, dynamic>> signInWithGoogle() async {
    try {
      // Déconnecte l'utilisateur précédent pour forcer le choix de compte en cas d'erreur passée
      await _googleSignIn.signOut();

      // 1. Ouvre le popup Google (Picker)
      final GoogleSignInAccount? googleUser = await _googleSignIn.signIn();
      if (googleUser == null) {
        return {'success': false, 'message': 'Connexion annulée par l\'utilisateur'};
      }

      // 2. Récupère les tokens depuis Google Play Services
      final GoogleSignInAuthentication googleAuth = await googleUser.authentication;
      final String? idToken = googleAuth.idToken;

      if (idToken == null) {
        debugPrint('⚠️ ERREUR: idToken est NULL. Le serverClientId configuré dans auth_service.dart DOIT être le Client ID "Application Web" de Google Cloud Console, PAS le Client ID "Android".');
        return {'success': false, 'message': 'Impossible de récupérer le token Google (Client ID Web invalide)'};
      }

      // 3. Envoie le idToken au backend Spring Boot
      final response = await http.post(
        Uri.parse('${ApiConfig.authUrl}/google'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({'idToken': idToken}),
      );

      return _handleAuthResponse(response);
    } catch (e) {
      debugPrint('Erreur Google Sign-In: $e');
      return {'success': false, 'message': 'Connexion impossible ($e)'};
    }
  }

  /// Méthode utilitaire pour traiter et sauvegarder la réponse du backend
  Future<Map<String, dynamic>> _handleAuthResponse(http.Response response) async {
    if (response.statusCode == 200 || response.statusCode == 201) {
      final data = jsonDecode(response.body);
      final token = data['token'];
      final role = data['role'] ?? 'patient';

      if (token != null) {
        await _storage.write(key: _tokenKey, value: token);
        await _storage.write(key: _roleKey, value: role);
      }

      return {'success': true, 'data': data, 'role': role};
    } else {
      final error = jsonDecode(response.body);
      return {'success': false, 'message': error['message'] ?? 'Erreur de connexion'};
    }
  }

  /// Envoyer OTP (Email Verification, Password Reset)
  Future<Map<String, dynamic>> sendOtp(String email, String purpose) async {
    try {
      final response = await http.post(
        Uri.parse('${ApiConfig.authUrl}/send-otp'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({'email': email, 'purpose': purpose}),
      );
      final data = jsonDecode(response.body);
      return {'success': response.statusCode == 200, 'message': data['message'] ?? (response.statusCode == 200 ? 'OTP envoyé' : 'Erreur')};
    } catch (e) {
      return {'success': false, 'message': 'Connexion impossible ($e)'};
    }
  }

  /// Vérifier OTP (Email Verification)
  Future<Map<String, dynamic>> verifyOtp(String email, String otp) async {
    try {
      final response = await http.post(
        Uri.parse('${ApiConfig.authUrl}/verify-otp'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({'email': email, 'otp': otp}),
      );
      final data = jsonDecode(response.body);
      return {
        'success': response.statusCode == 200, 
        'message': data['message'] ?? (response.statusCode == 200 ? 'Succès' : 'Erreur'),
        'status': data['status'],
        'role': data['role']
      };
    } catch (e) {
      return {'success': false, 'message': 'Connexion impossible ($e)'};
    }
  }

  /// Réinitialiser mot de passe en utilisant un OTP
  Future<Map<String, dynamic>> resetPassword(String email, String otp, String newPassword, String confirmPassword) async {
    try {
      final response = await http.post(
        Uri.parse('${ApiConfig.authUrl}/reset-password'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({
          'email': email, 
          'otp': otp, 
          'newPassword': newPassword, 
          'confirmPassword': confirmPassword
        }),
      );
      final data = jsonDecode(response.body);
      return {'success': response.statusCode == 200, 'message': data['message'] ?? 'Erreur'};
    } catch (e) {
      return {'success': false, 'message': 'Connexion impossible ($e)'};
    }
  }

  Future<void> logout() async {
    await _storage.deleteAll();
    await _googleSignIn.signOut();
  }

  Future<bool> isLoggedIn() async {
    final token = await _storage.read(key: _tokenKey);
    return token != null && token.isNotEmpty;
  }
}
