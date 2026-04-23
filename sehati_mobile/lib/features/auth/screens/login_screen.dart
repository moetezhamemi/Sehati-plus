import 'package:flutter/material.dart';
import 'package:sign_in_button/sign_in_button.dart';
import 'package:sehati_mobile/core/theme/app_colors.dart';
import 'package:sehati_mobile/features/auth/services/auth_service.dart';
import 'register_screen.dart';
import 'package:sehati_mobile/features/patient/screens/patient_dashboard_screen.dart';
import 'forgot_password_screen.dart';

class LoginScreen extends StatefulWidget {
  const LoginScreen({super.key});

  @override
  _LoginScreenState createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  final _formKey = GlobalKey<FormState>();
  final _emailController = TextEditingController();
  final _passwordController = TextEditingController();
  final AuthService _authService = AuthService();

  bool _obscurePassword = true;
  bool _isLoading = false;
  bool _isGoogleLoading = false;
  bool _rememberMe = false;
  String _errorMessage = '';

  void _handleRoleNavigation(String role) {
    if (!mounted) return;
    
    final String userRole = role.toLowerCase();
    
    // REDIRECTION ROLE-BASED
    if (userRole == 'medecin' || userRole == 'doctor') {
      Navigator.pushReplacement(context, MaterialPageRoute(builder: (_) => PatientDashboardScreen())); // TODO: Ecran Medecin
    } else if (userRole == 'admin') {
      Navigator.pushReplacement(context, MaterialPageRoute(builder: (_) => PatientDashboardScreen())); // TODO: Ecran Admin
    } else {
      Navigator.pushReplacement(context, MaterialPageRoute(builder: (_) => PatientDashboardScreen())); // Default: Patient
    }
  }

  void _login() async {
    if (_formKey.currentState!.validate()) {
      setState(() {
        _isLoading = true;
        _errorMessage = '';
      });

      final result = await _authService.login(
        _emailController.text,
        _passwordController.text,
      );

      if (!mounted) return;

      setState(() {
        _isLoading = false;
      });

      if (result['success']) {
        _handleRoleNavigation(result['role'] ?? 'patient');
      } else {
        setState(() {
          _errorMessage = result['message'];
        });
      }
    }
  }

  void _loginWithGoogle() async {
    setState(() {
      _isGoogleLoading = true;
      _errorMessage = '';
    });

    final result = await _authService.signInWithGoogle();

    if (!mounted) return;

    setState(() {
      _isGoogleLoading = false;
    });

    if (result['success']) {
      _handleRoleNavigation(result['role'] ?? 'patient');
    } else {
      if (result['message'] != 'Connexion annulée par l\'utilisateur') {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(result['message']),
            backgroundColor: AppColors.error,
          ),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.background,
      body: Center(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(24.0),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Text(
                'Bienvenue sur Sehhati+',
                textAlign: TextAlign.center,
                style: const TextStyle(
                  fontSize: 24,
                  fontWeight: FontWeight.bold,
                  color: AppColors.textDark,
                ),
              ),
              const SizedBox(height: 8),
              Text(
                'Connectez-vous pour accéder à votre espace',
                textAlign: TextAlign.center,
                style: TextStyle(
                  fontSize: 16,
                  color: AppColors.textDark,
                ),
              ),
              const SizedBox(height: 32),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 20),
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.circular(16),
                  boxShadow: [
                    BoxShadow(
                      color: Colors.black.withOpacity(0.05),
                      blurRadius: 10,
                      offset: const Offset(0, 4),
                    ),
                  ],
                ),
                child: Form(
                  key: _formKey,
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.stretch,
                    children: [
                      Text.rich(
                        TextSpan(
                          text: 'Adresse email ',
                          style: TextStyle(fontWeight: FontWeight.w500, color: AppColors.textDark),
                          children: [TextSpan(text: '*', style: TextStyle(color: AppColors.error))],
                        ),
                      ),
                      const SizedBox(height: 8),
                      TextFormField(
                        controller: _emailController,
                        decoration: InputDecoration(
                          hintText: 'Saisir votre email',
                          prefixIcon: Icon(Icons.email_outlined, color: AppColors.secondary),
                          border: OutlineInputBorder(
                            borderRadius: BorderRadius.circular(8),
                            borderSide: BorderSide(color: AppColors.secondary),
                          ),
                          enabledBorder: OutlineInputBorder(
                            borderRadius: BorderRadius.circular(8),
                            borderSide: BorderSide(color: AppColors.secondary),
                          ),
                          focusedBorder: OutlineInputBorder(
                            borderRadius: BorderRadius.circular(8),
                            borderSide: BorderSide(color: AppColors.primary),
                          ),
                          contentPadding: const EdgeInsets.symmetric(vertical: 16),
                        ),
                        keyboardType: TextInputType.emailAddress,
                        validator: (value) {
                          if (value == null || value.isEmpty) return 'Ce champ est obligatoire';
                          if (!value.contains('@')) return 'Format email invalide';
                          return null;
                        },
                      ),
                      const SizedBox(height: 20),
                      Text.rich(
                        TextSpan(
                          text: 'Mot de passe ',
                          style: TextStyle(fontWeight: FontWeight.w500, color: AppColors.textDark),
                          children: [TextSpan(text: '*', style: TextStyle(color: AppColors.error))],
                        ),
                      ),
                      const SizedBox(height: 8),
                      TextFormField(
                        controller: _passwordController,
                        obscureText: _obscurePassword,
                        decoration: InputDecoration(
                          hintText: '••••••••',
                          prefixIcon: Icon(Icons.lock_outline, color: AppColors.secondary),
                          suffixIcon: IconButton(
                            icon: Icon(
                              _obscurePassword ? Icons.visibility_off : Icons.visibility,
                              color: AppColors.secondary,
                            ),
                            onPressed: () {
                              setState(() {
                                _obscurePassword = !_obscurePassword;
                              });
                            },
                          ),
                          border: OutlineInputBorder(
                            borderRadius: BorderRadius.circular(8),
                            borderSide: BorderSide(color: AppColors.secondary),
                          ),
                          enabledBorder: OutlineInputBorder(
                            borderRadius: BorderRadius.circular(8),
                            borderSide: BorderSide(color: AppColors.secondary),
                          ),
                          focusedBorder: OutlineInputBorder(
                            borderRadius: BorderRadius.circular(8),
                            borderSide: BorderSide(color: AppColors.primary),
                          ),
                          contentPadding: const EdgeInsets.symmetric(vertical: 16),
                        ),
                        validator: (value) {
                          if (value == null || value.isEmpty) return 'Ce champ est obligatoire';
                          return null;
                        },
                      ),
                      const SizedBox(height: 10),
                      Row(
                        children: [
                          SizedBox(
                            height: 24,
                            width: 24,
                            child: Checkbox(
                              value: _rememberMe,
                              onChanged: (val) {
                                setState(() {
                                  _rememberMe = val ?? false;
                                });
                              },
                              activeColor: AppColors.primary,
                            ),
                          ),
                          const SizedBox(width: 8),
                          Expanded(
                            child: Text(
                              'Se souvenir de moi',
                              style: TextStyle(color: AppColors.textDark, fontSize: 12),
                              overflow: TextOverflow.ellipsis,
                            ),
                          ),
                          const SizedBox(width: 4),
                          TextButton(
                            onPressed: () {
                              Navigator.push(context, MaterialPageRoute(builder: (_) => const ForgotPasswordScreen()));
                            },
                            style: TextButton.styleFrom(padding: EdgeInsets.zero, minimumSize: const Size(0, 0)),
                            child: Text(
                              'Mot de passe oublié ?',
                              style: TextStyle(color: AppColors.primary, fontSize: 12),
                            ),
                          ),
                        ],
                      ),
                      if (_errorMessage.isNotEmpty)
                        Padding(
                          padding: const EdgeInsets.only(bottom: 16.0),
                          child: Text(
                            _errorMessage,
                            style: TextStyle(color: AppColors.error, fontSize: 14),
                            textAlign: TextAlign.center,
                          ),
                        ),
                      const SizedBox(height: 10),
                      ElevatedButton(
                        onPressed: (_isLoading || _isGoogleLoading) ? null : _login,
                        style: ElevatedButton.styleFrom(
                          backgroundColor: AppColors.primary,
                          foregroundColor: Colors.white,
                          padding: const EdgeInsets.symmetric(vertical: 16),
                          shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(8),
                          ),
                          elevation: 0,
                        ),
                        child: _isLoading
                            ? SizedBox(
                                height: 20,
                                width: 20,
                                child: CircularProgressIndicator(
                                  color: Colors.white,
                                  strokeWidth: 2,
                                ),
                              )
                            : Text(
                                'Se connecter',
                                style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                              ),
                      ),
                      const SizedBox(height: 24),
                      Row(
                        children: [
                          Expanded(child: Divider(color: AppColors.secondary)),
                          Padding(
                            padding: const EdgeInsets.symmetric(horizontal: 16),
                            child: Text('Ou continuer avec', style: TextStyle(color: AppColors.textDark, fontSize: 14)),
                          ),
                          Expanded(child: Divider(color: AppColors.secondary)),
                        ],
                      ),
                      const SizedBox(height: 20),
                      
                      // BOUTON OFFICIEL GOOGLE
                      _isGoogleLoading 
                        ? Center(
                            child: CircularProgressIndicator(
                              valueColor: AlwaysStoppedAnimation<Color>(Color(0xFF4285F4)),
                            ),
                          )
                        : SizedBox(
                            height: 50,
                            child: SignInButton(
                              Buttons.google,
                              text: "Continuer avec Google",
                              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                              onPressed: _isLoading ? () {} : _loginWithGoogle,
                            ),
                          ),

                      const SizedBox(height: 24),
                      Wrap(
                        alignment: WrapAlignment.center,
                        crossAxisAlignment: WrapCrossAlignment.center,
                        children: [
                          Text(
                            'Vous n\'avez pas de compte ?',
                            style: TextStyle(color: AppColors.textDark),
                          ),
                          TextButton(
                            onPressed: () {
                              Navigator.push(
                                context,
                                MaterialPageRoute(builder: (context) => RegisterScreen()),
                              );
                            },
                            child: Text(
                              'S\'inscrire',
                              style: TextStyle(
                                color: AppColors.primary,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ),
                        ],
                      ),
                    ],
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
