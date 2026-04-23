const { execSync } = require('child_process');
const fs = require('fs');

const restore = (gitPath, destPath) => {
    try {
        const content = execSync(`git show "HEAD:${gitPath}"`, {cwd: 'c:/Sehati+/sehati_mobile'});
        fs.writeFileSync(`c:/Sehati+/sehati_mobile/${destPath}`, content);
        console.log(`Restored ${gitPath} to ${destPath}`);
    } catch(e) {
        console.error(`Failed to restore ${gitPath}:`, e.message);
    }
}

restore('sehati_mobile/lib/screens/register_screen.dart', 'lib/features/auth/screens/register_screen.dart');
restore('sehati_mobile/lib/screens/auth/otp_verification_screen.dart', 'lib/features/auth/screens/otp_verification_screen.dart');
restore('sehati_mobile/lib/screens/auth/reset_password_screen.dart', 'lib/features/auth/screens/reset_password_screen.dart');
restore('sehati_mobile/lib/main.dart', 'lib/main.dart');
