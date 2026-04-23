import 'package:sehati_mobile/core/theme/app_colors.dart';
import 'package:flutter/material.dart';
import 'package:sehati_mobile/features/auth/services/auth_service.dart';
import 'login_screen.dart';

class ResetPasswordScreen extends StatefulWidget {
  final String email;

  const ResetPasswordScreen({super.key, required this.email});


  @override
  _ResetPasswordScreenState createState() => _ResetPasswordScreenState();
}

class _ResetPasswordScreenState extends State<ResetPasswordScreen> {
  final _otpController = TextEditingController();
  final _focusNode = FocusNode();
  final _passwordController = TextEditingController();
  final _confirmPasswordController = TextEditingController();
  final _authService = AuthService();

  bool _isLoading = false;
  String _errorMessage = '';
  bool _obscurePassword = true;
  bool _obscureConfirm = true;

  @override
  void initState() {
    super.initState();
    // Focus on OTP field when screen opens
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _focusNode.requestFocus();
    });
  }

  @override
  void dispose() {
    _otpController.dispose();
    _focusNode.dispose();
    _passwordController.dispose();
    _confirmPasswordController.dispose();
    super.dispose();
  }

  Future<void> _resetPassword() async {
    final otp = _otpController.text.trim();
    final password = _passwordController.text;
    final confirm = _confirmPasswordController.text;

    if (otp.length != 6) {
      setState(() => _errorMessage = 'Veuillez saisir les 6 chiffres du code OTP.');
      return;
    }
    if (password.length < 8) {
      setState(() => _errorMessage = 'Le mot de passe doit contenir au moins 8 caractères.');
      return;
    }
    if (password != confirm) {
      setState(() => _errorMessage = 'Les mots de passe ne correspondent pas.');
      return;
    }

    setState(() {
      _isLoading = true;
      _errorMessage = '';
    });

    final result = await _authService.resetPassword(widget.email, otp, password, confirm);
    setState(() => _isLoading = false);

    if (result['success']) {
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Mot de passe mis à jour avec succès.')));
      Navigator.of(context).pushAndRemoveUntil(
        MaterialPageRoute(builder: (context) => LoginScreen()),
        (Route<dynamic> route) => false,
      );
    } else {
      setState(() => _errorMessage = result['message']);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF5F9FF),
      appBar: AppBar(
        title: Text('Réinitialiser le mot de passe'),
        backgroundColor: Colors.white,
        elevation: 1,
        iconTheme: IconThemeData(color: Colors.black),
        titleTextStyle: TextStyle(color: Colors.black, fontSize: 18, fontWeight: FontWeight.bold),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(24.0),
        child: Column(
          children: [
            SizedBox(height: 20),
            Text('Entrez le code à 6 chiffres envoyé à ${widget.email} et choisissez un nouveau mot de passe.', textAlign: TextAlign.center, style: TextStyle(fontSize: 15)),
            SizedBox(height: 30),
            GestureDetector(
              onTap: () => _focusNode.requestFocus(),
              child: Stack(
                alignment: Alignment.center,
                children: [
                  // Visual Boxes
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                    children: List.generate(6, (index) {
                      String char = "";
                      if (_otpController.text.length > index) {
                        char = _otpController.text[index];
                      }
                      
                      bool isFocused = _otpController.text.length == index;
                      bool isFilled = index < _otpController.text.length;

                      return Container(
                        width: 40, // Slightly smaller for reset screen to fit alongside other fields
                        height: 50,
                        alignment: Alignment.center,
                        decoration: BoxDecoration(
                          color: isFilled ? AppColors.background : Colors.white,
                          borderRadius: BorderRadius.circular(10),
                          border: Border.all(
                            color: isFocused ? const Color(0xFF87B7E8) : (isFilled ? const Color(0xFF87B7E8) : AppColors.secondary),
                            width: isFocused ? 2 : 1.5,
                          ),
                          boxShadow: [
                            if (isFocused)
                              BoxShadow(
                                color: const Color(0xFF87B7E8).withOpacity(0.15),
                                blurRadius: 8,
                                offset: const Offset(0, 4),
                              ),
                          ],
                        ),
                        child: Text(
                          char,
                          style: const TextStyle(
                            fontSize: 22,
                            fontWeight: FontWeight.bold,
                            color: Color(0xFF1F2937),
                          ),
                        ),
                      );
                    }),
                  ),
                  // Hidden Real Input
                  Opacity(
                    opacity: 0,
                    child: TextField(
                      controller: _otpController,
                      focusNode: _focusNode,
                      keyboardType: TextInputType.number,
                      maxLength: 6,
                      onChanged: (value) {
                        setState(() {});
                      },
                      decoration: const InputDecoration(
                        counterText: "",
                      ),
                    ),
                  ),
                ],
              ),
            ),
            SizedBox(height: 20),
            TextField(
              controller: _passwordController,
              obscureText: _obscurePassword,
              decoration: InputDecoration(
                labelText: 'Nouveau mot de passe',
                prefixIcon: Icon(Icons.lock_outline),
                suffixIcon: IconButton(
                  icon: Icon(_obscurePassword ? Icons.visibility_off : Icons.visibility),
                  onPressed: () => setState(() => _obscurePassword = !_obscurePassword),
                ),
                border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
              ),
            ),
            SizedBox(height: 20),
            TextField(
              controller: _confirmPasswordController,
              obscureText: _obscureConfirm,
              decoration: InputDecoration(
                labelText: 'Confirmer le mot de passe',
                prefixIcon: Icon(Icons.lock_outline),
                suffixIcon: IconButton(
                  icon: Icon(_obscureConfirm ? Icons.visibility_off : Icons.visibility),
                  onPressed: () => setState(() => _obscureConfirm = !_obscureConfirm),
                ),
                border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
              ),
            ),
            SizedBox(height: 10),
            if (_errorMessage.isNotEmpty)
              Text(_errorMessage, style: TextStyle(color: AppColors.error)),
            SizedBox(height: 30),
            ElevatedButton(
              onPressed: _isLoading ? null : _resetPassword,
              style: ElevatedButton.styleFrom(
                backgroundColor: Color(0xFF2A7DE1),
                minimumSize: Size(double.infinity, 50),
              ),
              child: _isLoading ? CircularProgressIndicator(color: Colors.white) : Text('Réinitialiser', style: TextStyle(color: Colors.white, fontSize: 16)),
            ),
          ],
        ),
      ),
    );
  }
}
