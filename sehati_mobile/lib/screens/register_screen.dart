import 'package:sehati_mobile/theme/app_colors.dart';
import 'package:flutter/material.dart';
import '../services/auth_service.dart';
import 'auth/otp_verification_screen.dart';

class RegisterScreen extends StatefulWidget {
  @override
  _RegisterScreenState createState() => _RegisterScreenState();
}
class _RegisterScreenState extends State<RegisterScreen> {
  final _formKey = GlobalKey<FormState>();
  final _prenomController = TextEditingController();
  final _nomController = TextEditingController();
  final _emailController = TextEditingController();
  final _phoneController = TextEditingController();
  final _dateNaissanceController = TextEditingController();
  String _dateNaissanceISO = ''; // Format YYYY-MM-DD pour le backend
  final _passwordController = TextEditingController();
  final _confirmPasswordController = TextEditingController();
  
  final AuthService _authService = AuthService();

  bool _obscurePassword = true;
  bool _obscureConfirmPassword = true;
  bool _isLoading = false;
  String _errorMessage = '';
  bool _acceptTerms = false;

  void _register() async {
    if (_formKey.currentState!.validate() && _acceptTerms) {
      if (_passwordController.text != _confirmPasswordController.text) {
        setState(() {
          _errorMessage = 'Les mots de passe ne correspondent pas';
        });
        return;
      }

      setState(() {
        _isLoading = true;
        _errorMessage = '';
      });

      final result = await _authService.registerPatient(
        _prenomController.text,
        _nomController.text,
        _emailController.text,
        _phoneController.text,
        _passwordController.text,
        _confirmPasswordController.text, // Added Confirm Password for Backend
        _dateNaissanceISO,
      );

      setState(() {
        _isLoading = false;
      });

      if (result['success']) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(result['message'])),
        );
        Navigator.pushReplacement(
          context,
          MaterialPageRoute(
            builder: (context) => OtpVerificationScreen(email: _emailController.text.trim()),
          ),
        );
      } else {
        setState(() {
          _errorMessage = result['message'];
        });
      }
    } else if (!_acceptTerms) {
      setState(() {
        _errorMessage = 'Veuillez accepter les conditions d\'utilisation';
      });
    }
  }

  Widget _buildTextField(String label, String hint, IconData icon, TextEditingController controller, {bool isEmail = false, bool isPhone = false}) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Text.rich(
          TextSpan(
            text: label + ' ',
            style: TextStyle(fontWeight: FontWeight.w500, color: AppColors.textDark),
            children: [TextSpan(text: '*', style: TextStyle(color: AppColors.error))],
          ),
        ),
        const SizedBox(height: 8),
        TextFormField(
          controller: controller,
          keyboardType: isEmail ? TextInputType.emailAddress : (isPhone ? TextInputType.phone : TextInputType.text),
          decoration: InputDecoration(
            hintText: hint,
            prefixIcon: Icon(icon, color: AppColors.secondary),
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
              borderSide: BorderSide(color: Color(0xFF2A7DE1)),
            ),
            contentPadding: const EdgeInsets.symmetric(vertical: 16),
          ),
          validator: (value) {
            if (value == null || value.isEmpty) return 'Ce champ est obligatoire';
            if (isEmail && !value.contains('@')) return 'Veuillez saisir un email valide';
            return null;
          },
        ),
        const SizedBox(height: 16),
      ],
    );
  }

  Widget _buildDatePickerField() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Text.rich(
          TextSpan(
            text: 'Date de naissance ',
            style: TextStyle(fontWeight: FontWeight.w500, color: AppColors.textDark),
            children: [TextSpan(text: '*', style: TextStyle(color: AppColors.error))],
          ),
        ),
        const SizedBox(height: 8),
        TextFormField(
          controller: _dateNaissanceController,
          readOnly: true,
          decoration: InputDecoration(
            hintText: 'jj/mm/aaaa',
            prefixIcon: Icon(Icons.calendar_today_outlined, color: AppColors.secondary),
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
              borderSide: BorderSide(color: Color(0xFF2A7DE1)),
            ),
            contentPadding: const EdgeInsets.symmetric(vertical: 16),
          ),
          onTap: () async {
            DateTime? pickedDate = await showDatePicker(
              context: context,
              initialDate: DateTime.now().subtract(const Duration(days: 365 * 18)), // Default to 18 years old
              firstDate: DateTime(1900),
              lastDate: DateTime.now(),
              builder: (context, child) {
                return Theme(
                  data: Theme.of(context).copyWith(
                    colorScheme: ColorScheme.light(
                      primary: Color(0xFF2A7DE1), // header background color
                      onPrimary: Colors.white, // header text color
                      onSurface: Colors.black, // body text color
                    ),
                  ),
                  child: child!,
                );
              },
            );

            if (pickedDate != null) {
              // Format affiché dans l'interface : JJ/MM/AAAA
              String displayDate =
                  "${pickedDate.day.toString().padLeft(2, '0')}/${pickedDate.month.toString().padLeft(2, '0')}/${pickedDate.year}";
              // Format envoyé au backend : YYYY-MM-DD
              _dateNaissanceISO =
                  "${pickedDate.year}-${pickedDate.month.toString().padLeft(2, '0')}-${pickedDate.day.toString().padLeft(2, '0')}";
              setState(() {
                _dateNaissanceController.text = displayDate;
              });
            }
          },
          validator: (value) {
            if (value == null || value.isEmpty) return 'Ce champ est obligatoire';
            return null;
          },
        ),
        const SizedBox(height: 16),
      ],
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF5F9FF),
      appBar: AppBar(
        backgroundColor: const Color(0xFFF5F9FF),
        elevation: 0,
        leading: IconButton(
          icon: Icon(Icons.arrow_back, color: AppColors.textDark),
          onPressed: () => Navigator.pop(context),
        ),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(24.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Text(
              'Inscription Patient',
              textAlign: TextAlign.center,
              style: TextStyle(
                fontSize: 28,
                fontWeight: FontWeight.bold,
                color: AppColors.textDark,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              'Créez votre compte Sehhati+ en quelques étapes',
              textAlign: TextAlign.center,
              style: TextStyle(
                fontSize: 16,
                color: AppColors.textDark,
              ),
            ),
            const SizedBox(height: 32),
            Container(
              padding: const EdgeInsets.all(24),
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
                    Row(
                      children: [
                        Expanded(child: _buildTextField('Prénom', 'Prénom', Icons.person_outline, _prenomController)),
                        const SizedBox(width: 16),
                        Expanded(child: _buildTextField('Nom', 'Nom', Icons.person_outline, _nomController)),
                      ],
                    ),
                    
                    _buildTextField('Email', 'Saisir votre email', Icons.email_outlined, _emailController, isEmail: true),
                    _buildTextField('Téléphone', '+ 216', Icons.phone_outlined, _phoneController, isPhone: true),
                    _buildDatePickerField(),
                    
                    // Password
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
                        hintText: 'Mot de passe',
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
                        ),
                        enabledBorder: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(8),
                          borderSide: BorderSide(color: AppColors.secondary),
                        ),
                        focusedBorder: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(8),
                          borderSide: BorderSide(color: Color(0xFF2A7DE1)),
                        ),
                      ),
                      validator: (value) {
                        if (value == null || value.isEmpty) return 'Ce champ est obligatoire';
                        if (!RegExp(r'^(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$').hasMatch(value)) {
                          return 'Le mot de passe doit contenir au moins 8 caractères, dont une majuscule, un chiffre et un caractère spécial.';
                        }
                        return null;
                      },
                    ),
                    const SizedBox(height: 16),

                    // Confirm Password
                    Text.rich(
                      TextSpan(
                        text: 'Confirmer le mot de passe ',
                        style: TextStyle(fontWeight: FontWeight.w500, color: AppColors.textDark),
                        children: [TextSpan(text: '*', style: TextStyle(color: AppColors.error))],
                      ),
                    ),
                    const SizedBox(height: 8),
                    TextFormField(
                      controller: _confirmPasswordController,
                      obscureText: _obscureConfirmPassword,
                      decoration: InputDecoration(
                        hintText: 'Confirmer',
                        prefixIcon: Icon(Icons.lock_outline, color: AppColors.secondary),
                        suffixIcon: IconButton(
                          icon: Icon(
                            _obscureConfirmPassword ? Icons.visibility_off : Icons.visibility,
                            color: AppColors.secondary,
                          ),
                          onPressed: () {
                            setState(() {
                              _obscureConfirmPassword = !_obscureConfirmPassword;
                            });
                          },
                        ),
                        border: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(8),
                        ),
                        enabledBorder: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(8),
                          borderSide: BorderSide(color: AppColors.secondary),
                        ),
                        focusedBorder: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(8),
                          borderSide: BorderSide(color: Color(0xFF2A7DE1)),
                        ),
                      ),
                      validator: (value) {
                        if (value == null || value.isEmpty) return 'Ce champ est obligatoire';
                        return null;
                      },
                    ),
                    const SizedBox(height: 24),

                    CheckboxListTile(
                      value: _acceptTerms,
                      onChanged: (val) {
                        setState(() {
                          _acceptTerms = val ?? false;
                        });
                      },
                      title: Text(
                        'J\'accepte les conditions d\'utilisation et la politique de confidentialité',
                        style: TextStyle(fontSize: 14, color: AppColors.textDark),
                      ),
                      controlAffinity: ListTileControlAffinity.leading,
                      contentPadding: EdgeInsets.zero,
                      activeColor: Color(0xFF2A7DE1),
                    ),

                    if (_errorMessage.isNotEmpty)
                      Padding(
                        padding: const EdgeInsets.only(bottom: 16.0, top: 8.0),
                        child: Text(
                          _errorMessage,
                          style: TextStyle(color: AppColors.error, fontSize: 14),
                          textAlign: TextAlign.center,
                        ),
                      ),
                      
                    const SizedBox(height: 16),
                    ElevatedButton(
                      onPressed: _isLoading ? null : _register,
                      style: ElevatedButton.styleFrom(
                        backgroundColor: Color(0xFF2A7DE1),
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
                              'S\'inscrire',
                              style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                            ),
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
