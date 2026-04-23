import 'package:sehati_mobile/core/theme/app_colors.dart';
import 'package:flutter/material.dart';
import 'package:sehati_mobile/features/auth/services/auth_service.dart';
import 'dart:async';
import 'login_screen.dart';

class OtpVerificationScreen extends StatefulWidget {
  final String email;

  const OtpVerificationScreen({super.key, required this.email});

  @override
  _OtpVerificationScreenState createState() => _OtpVerificationScreenState();
}

class _OtpVerificationScreenState extends State<OtpVerificationScreen> {
  final _otpController = TextEditingController();
  final _focusNode = FocusNode();
  final _authService = AuthService();
  bool _isLoading = false;
  String _errorMessage = '';
  int _cooldown = 0;
  Timer? _timer;

  @override
  void initState() {
    super.initState();
    // Auto focus when screen opens
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _focusNode.requestFocus();
    });
  }

  @override
  void dispose() {
    _otpController.dispose();
    _focusNode.dispose();
    _timer?.cancel();
    super.dispose();
  }

  void _startCooldown() {
    setState(() => _cooldown = 60);
    _timer = Timer.periodic(Duration(seconds: 1), (timer) {
      if (_cooldown > 0) {
        setState(() => _cooldown--);
      } else {
        timer.cancel();
      }
    });
  }

  Future<void> _resendOtp() async {
    if (_cooldown > 0) return;
    
    setState(() => _isLoading = true);
    final result = await _authService.sendOtp(widget.email, 'EMAIL_VERIFICATION');
    setState(() => _isLoading = false);

    if (result['success']) {
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Code renvoyé')));
      _startCooldown();
    } else {
      setState(() => _errorMessage = result['message']);
    }
  }

  Future<void> _verifyOtp() async {
    final otp = _otpController.text.trim();
    if (otp.length != 6) {
      setState(() => _errorMessage = 'Veuillez saisir les 6 chiffres.');
      return;
    }

    setState(() {
      _isLoading = true;
      _errorMessage = '';
    });

    final result = await _authService.verifyOtp(widget.email, otp);
    setState(() => _isLoading = false);

    if (result['success']) {
      // Naviguer vers le dashboard ou la page de connexion
      final status = result['status'];
      if (status == 'APPROVED') {
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Compte vérifié avec succès. Connectez-vous.')));
        Navigator.of(context).pushAndRemoveUntil(
          MaterialPageRoute(builder: (context) => LoginScreen()),
          (Route<dynamic> route) => false,
        );
      } else {
        showDialog(
          context: context,
          builder: (ctx) => AlertDialog(
            title: Text('Vérification réussie'),
            content: Text('Votre compte est en attente d\'approbation par l\'administrateur.'),
            actions: [
              TextButton(
                onPressed: () {
                  Navigator.of(context).pushAndRemoveUntil(
                    MaterialPageRoute(builder: (context) => LoginScreen()),
                    (Route<dynamic> route) => false,
                  );
                },
                child: Text('OK'),
              )
            ],
          )
        );
      }
    } else {
      setState(() => _errorMessage = result['message']);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF5F9FF),
      appBar: AppBar(
        title: Text('Vérification'),
        backgroundColor: Colors.white,
        elevation: 1,
        iconTheme: IconThemeData(color: Colors.black),
        titleTextStyle: TextStyle(color: Colors.black, fontSize: 18, fontWeight: FontWeight.bold),
      ),
      body: Padding(
        padding: const EdgeInsets.all(24.0),
        child: Column(
          children: [
            SizedBox(height: 20),
            Text('Un code de vérification a été envoyé à', textAlign: TextAlign.center, style: TextStyle(fontSize: 16)),
            Text(widget.email, textAlign: TextAlign.center, style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: Color(0xFF87B7E8))),
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
                        width: 45,
                        height: 55,
                        alignment: Alignment.center,
                        decoration: BoxDecoration(
                          color: isFilled ? AppColors.background : Colors.white,
                          borderRadius: BorderRadius.circular(12),
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
                            fontSize: 24,
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
                        if (value.length == 6) {
                          _verifyOtp();
                        }
                      },
                      decoration: const InputDecoration(
                        counterText: "",
                      ),
                    ),
                  ),
                ],
              ),
            ),
            SizedBox(height: 10),
            if (_errorMessage.isNotEmpty)
              Text(_errorMessage, style: TextStyle(color: AppColors.error)),
            SizedBox(height: 20),
            ElevatedButton(
              onPressed: _isLoading ? null : _verifyOtp,
              style: ElevatedButton.styleFrom(
                backgroundColor: Color(0xFF2A7DE1),
                minimumSize: Size(double.infinity, 50),
              ),
              child: _isLoading ? CircularProgressIndicator(color: Colors.white) : Text('Vérifier', style: TextStyle(color: Colors.white, fontSize: 16)),
            ),
            SizedBox(height: 20),
            TextButton(
              onPressed: _cooldown > 0 ? null : _resendOtp,
              child: Text(_cooldown > 0 ? 'Renvoyer le code dans ${_cooldown}s' : 'Renvoyer le code'),
            )
          ],
        ),
      ),
    );
  }
}
