const fs = require('fs');
const fix = (file, oldStr, newStr) => {
    try {
        let content = fs.readFileSync(file, 'utf8');
        content = content.replace(oldStr, newStr);
        fs.writeFileSync(file, content, 'utf8');
        console.log(`Fixed ${file}`);
    } catch(e) { console.error(`Error on ${file}: ${e.message}`); }
}

fix('c:/Sehati+/sehati_mobile/lib/features/auth/screens/register_screen.dart', "import 'auth/otp_verification_screen.dart';", "import 'otp_verification_screen.dart';");
fix('c:/Sehati+/sehati_mobile/lib/features/auth/screens/otp_verification_screen.dart', "import '../login_screen.dart';", "import 'login_screen.dart';");
fix('c:/Sehati+/sehati_mobile/lib/features/auth/screens/reset_password_screen.dart', "import '../login_screen.dart';", "import 'login_screen.dart';");
fix('c:/Sehati+/sehati_mobile/lib/main.dart', "import 'screens/login_screen.dart';", "import 'package:sehati_mobile/features/auth/screens/login_screen.dart';");
