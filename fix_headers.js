const fs = require('fs');

function prepend(file, header) {
    let content = fs.readFileSync(file, 'utf8');
    // Clean up any stray first lines that might be leftover pieces
    if (content.startsWith("import 'package:sehati_mobile/core/theme/app_colors.dart';\n")) {
        content = content.replace("import 'package:sehati_mobile/core/theme/app_colors.dart';\n", "");
    }
    fs.writeFileSync(file, header + content, 'utf8');
    console.log("Fixed", file);
}

const regHeader = `import 'package:sehati_mobile/core/theme/app_colors.dart';
import 'package:flutter/material.dart';
import 'package:sehati_mobile/features/auth/services/auth_service.dart';
import 'otp_verification_screen.dart';

class RegisterScreen extends StatefulWidget {
  const RegisterScreen({Key? key}) : super(key: key);
  @override
  _RegisterScreenState createState() => _RegisterScreenState();
}
`;
prepend('c:/Sehati+/sehati_mobile/lib/features/auth/screens/register_screen.dart', regHeader);

const otpHeader = `import 'package:sehati_mobile/core/theme/app_colors.dart';
import 'package:flutter/material.dart';
import 'package:sehati_mobile/features/auth/services/auth_service.dart';
import 'dart:async';
import 'login_screen.dart';

class OtpVerificationScreen extends StatefulWidget {
  final String email;

`;
prepend('c:/Sehati+/sehati_mobile/lib/features/auth/screens/otp_verification_screen.dart', otpHeader);

const resetHeader = `import 'package:sehati_mobile/core/theme/app_colors.dart';
import 'package:flutter/material.dart';
import 'package:sehati_mobile/features/auth/services/auth_service.dart';
import 'login_screen.dart';

class ResetPasswordScreen extends StatefulWidget {
  final String email;

  const ResetPasswordScreen({Key? key, required this.email}) : super(key: key);

`;
prepend('c:/Sehati+/sehati_mobile/lib/features/auth/screens/reset_password_screen.dart', resetHeader);
