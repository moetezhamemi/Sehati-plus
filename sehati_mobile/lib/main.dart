import 'package:flutter/material.dart';
import 'package:sehati_mobile/theme/app_colors.dart';
import 'screens/login_screen.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Sehhati+',
      debugShowCheckedModeBanner: false,
      showPerformanceOverlay: false,
      showSemanticsDebugger: false,
      debugShowMaterialGrid: false,
      theme: ThemeData(
        primaryColor: const Color(0xFF2A7DE1),
        scaffoldBackgroundColor: const Color(0xFFF5F9FF),
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color(0xFF2A7DE1),
          primary: const Color(0xFF2A7DE1),
          secondary: const Color(0xFF87B7E8),
          surface: const Color(0xFFF5F9FF),
          error: const Color(0xFFEA5455),
          onPrimary: Colors.white,
          onSecondary: Colors.black,
          onSurface: AppColors.textDark,
          onError: Colors.white,
        ),
        useMaterial3: true,
        fontFamily: 'Inter',
        appBarTheme: const AppBarTheme(
          backgroundColor: Color(0xFF2A7DE1),
          foregroundColor: Colors.white,
        ),
        elevatedButtonTheme: ElevatedButtonThemeData(
          style: ElevatedButton.styleFrom(
            backgroundColor: const Color(0xFF2A7DE1),
            foregroundColor: Colors.white,
          ),
        ),
        textButtonTheme: TextButtonThemeData(
          style: TextButton.styleFrom(
            foregroundColor: const Color(0xFF2A7DE1),
          ),
        ),
      ),
      home: LoginScreen(),
    );
  }
}
