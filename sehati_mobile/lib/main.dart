import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:sehati_mobile/features/auth/screens/login_screen.dart';
import 'package:sehati_mobile/core/theme/app_colors.dart';
import 'package:sehati_mobile/features/auth/services/auth_service.dart';
import 'package:sehati_mobile/features/patient/screens/patient_dashboard_screen.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Sehati+',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: AppColors.primary),
        useMaterial3: true,
        scaffoldBackgroundColor: AppColors.background,
        textTheme: GoogleFonts.interTextTheme(Theme.of(context).textTheme),
      ),
      home: FutureBuilder<bool>(
        future: AuthService().isLoggedIn(),
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const Scaffold(
              body: Center(child: CircularProgressIndicator()),
            );
          }
          if (snapshot.hasData && snapshot.data == true) {
            // Utilisateur connecté, on redirige vers le dashboard patient par défaut
            return const PatientDashboardScreen();
          }
          // Non connecté
          return LoginScreen();
        },
      ),
    );
  }
}
