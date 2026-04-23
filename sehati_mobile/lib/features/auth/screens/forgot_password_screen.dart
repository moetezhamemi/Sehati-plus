import 'package:sehati_mobile/core/theme/app_colors.dart';
import 'package:flutter/material.dart';
import 'package:sehati_mobile/features/auth/services/auth_service.dart';
import 'reset_password_screen.dart';

class ForgotPasswordScreen extends StatefulWidget {
  const ForgotPasswordScreen({super.key});

  @override
  _ForgotPasswordScreenState createState() => _ForgotPasswordScreenState();
}

class _ForgotPasswordScreenState extends State<ForgotPasswordScreen> {
  final _formKey = GlobalKey<FormState>();
  final _emailController = TextEditingController();
  final AuthService _authService = AuthService();
  bool _isLoading = false;
  bool _emailSent = false;
  String _errorMessage = '';

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() {
      _isLoading = true;
      _errorMessage = '';
    });

    try {
      final result = await _authService.forgotPassword(_emailController.text.trim());

      if (!mounted) return;
      if (result['success']) {
        setState(() { _isLoading = false; _emailSent = true; });
      } else {
        setState(() {
          _isLoading = false;
          _errorMessage = result['message'];
        });
      }
    } catch (e) {
      if (mounted) setState(() { _isLoading = false; _errorMessage = 'Impossible de joindre le serveur.'; });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF5F9FF),
      appBar: AppBar(
        backgroundColor: Colors.white,
        elevation: 1,
        leading: IconButton(
          icon: Icon(Icons.arrow_back, color: AppColors.textDark),
          onPressed: () => Navigator.pop(context),
        ),
        title: Text('Mot de passe oublié', style: TextStyle(color: AppColors.textDark, fontWeight: FontWeight.w600, fontSize: 18)),
      ),
      body: Center(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(24),
          child: _emailSent ? _buildSuccess() : _buildForm(),
        ),
      ),
    );
  }

  Widget _buildSuccess() {
    return Column(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        Container(
          width: 72, height: 72,
          decoration: BoxDecoration(color: AppColors.background, shape: BoxShape.circle),
          child: Icon(Icons.mark_email_read_outlined, color: Color(0xFF87B7E8), size: 36),
        ),
        const SizedBox(height: 24),
        Text('Email envoyé !', style: TextStyle(fontSize: 22, fontWeight: FontWeight.bold, color: AppColors.textDark)),
        const SizedBox(height: 12),
        Text(
          'Si un compte existe pour ${_emailController.text}, vous recevrez un code de réinitialisation.',
          textAlign: TextAlign.center,
          style: TextStyle(color: AppColors.textDark, fontSize: 15, height: 1.5),
        ),
        const SizedBox(height: 32),
        ElevatedButton(
          onPressed: () {
            Navigator.pushReplacement(
              context,
              MaterialPageRoute(builder: (context) => ResetPasswordScreen(email: _emailController.text.trim())),
            );
          },
          style: ElevatedButton.styleFrom(
            backgroundColor: Color(0xFF2A7DE1),
            padding: const EdgeInsets.symmetric(vertical: 14, horizontal: 32),
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
            elevation: 0,
          ),
          child: Text('Entrer le code', style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold)),
        ),
        const SizedBox(height: 16),
        OutlinedButton(
          onPressed: () => Navigator.pop(context),
          style: OutlinedButton.styleFrom(
            padding: const EdgeInsets.symmetric(vertical: 14, horizontal: 32),
            side: BorderSide(color: Color(0xFF2A7DE1)),
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
          ),
          child: Text('Retour à la connexion', style: TextStyle(color: Color(0xFF87B7E8), fontWeight: FontWeight.w600)),
        ),
      ],
    );
  }

  Widget _buildForm() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Container(
          width: 72, height: 72,
          margin: const EdgeInsets.only(bottom: 24),
          decoration: BoxDecoration(color: AppColors.background, shape: BoxShape.circle),
          child: Center(child: Icon(Icons.lock_reset, color: Color(0xFF87B7E8), size: 36)),
        ),
        Text('Réinitialiser le mot de passe', style: TextStyle(fontSize: 22, fontWeight: FontWeight.bold, color: AppColors.textDark), textAlign: TextAlign.center),
        const SizedBox(height: 8),
        Text('Entrez votre email, on vous enverra un code pour créer un nouveau mot de passe.', style: TextStyle(color: AppColors.textDark, fontSize: 14, height: 1.5), textAlign: TextAlign.center),
        const SizedBox(height: 32),
        Container(
          padding: const EdgeInsets.all(24),
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(16),
            boxShadow: [BoxShadow(color: Colors.black.withOpacity(0.05), blurRadius: 10, offset: Offset(0, 4))],
          ),
          child: Form(
            key: _formKey,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                Text.rich(TextSpan(text: 'Adresse email ', style: TextStyle(fontWeight: FontWeight.w500, color: AppColors.textDark), children: [TextSpan(text: '*', style: TextStyle(color: AppColors.error))])),
                const SizedBox(height: 8),
                TextFormField(
                  controller: _emailController,
                  keyboardType: TextInputType.emailAddress,
                  decoration: InputDecoration(
                    hintText: 'Votre email',
                    prefixIcon: Icon(Icons.email_outlined, color: AppColors.secondary),
                    enabledBorder: OutlineInputBorder(borderRadius: BorderRadius.circular(8), borderSide: BorderSide(color: AppColors.secondary)),
                    focusedBorder: OutlineInputBorder(borderRadius: BorderRadius.circular(8), borderSide: BorderSide(color: Color(0xFF2A7DE1))),
                    errorBorder: OutlineInputBorder(borderRadius: BorderRadius.circular(8), borderSide: BorderSide(color: AppColors.error)),
                    focusedErrorBorder: OutlineInputBorder(borderRadius: BorderRadius.circular(8), borderSide: BorderSide(color: AppColors.error)),
                    contentPadding: const EdgeInsets.symmetric(vertical: 16),
                  ),
                  validator: (v) {
                    if (v == null || v.isEmpty) return 'Ce champ est obligatoire';
                    if (!v.contains('@')) return 'Veuillez saisir un email valide';
                    return null;
                  },
                ),
                if (_errorMessage.isNotEmpty) ...[
                  const SizedBox(height: 12),
                  Text(_errorMessage, style: TextStyle(color: AppColors.error, fontSize: 13), textAlign: TextAlign.center),
                ],
                const SizedBox(height: 20),
                ElevatedButton(
                  onPressed: _isLoading ? null : _submit,
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Color(0xFF2A7DE1),
                    foregroundColor: Colors.white,
                    padding: const EdgeInsets.symmetric(vertical: 16),
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                    elevation: 0,
                  ),
                  child: _isLoading
                      ? SizedBox(height: 20, width: 20, child: CircularProgressIndicator(color: Colors.white, strokeWidth: 2))
                      : Text('Envoyer le code', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
                ),
              ],
            ),
          ),
        ),
      ],
    );
  }
}
