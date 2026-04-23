const fs = require('fs');
const path = require('path');

const libDir = 'c:/Sehati+/sehati_mobile/lib';
// 1. Create directories
const dirsToCreate = [
  'core', 'core/theme', 'core/config',
  'features', 'features/auth', 'features/auth/screens', 'features/auth/services',
  'features/patient', 'features/patient/screens'
];
for (const dir of dirsToCreate) {
  const fullPath = path.join(libDir, dir);
  if (!fs.existsSync(fullPath)) fs.mkdirSync(fullPath, { recursive: true });
}

function moveFolderSync(from, to) {
    if (fs.existsSync(from)) {
        if (!fs.existsSync(to)) fs.mkdirSync(to, { recursive: true });
        const files = fs.readdirSync(from);
        for (const file of files) {
            const fromPath = path.join(from, file);
            const toPath = path.join(to, file);
            if (fs.statSync(fromPath).isDirectory()) {
                moveFolderSync(fromPath, toPath);
            } else {
                fs.renameSync(fromPath, toPath);
            }
        }
        fs.rmdirSync(from);
    }
}

// 2. Move files/folders
moveFolderSync(path.join(libDir, 'theme'), path.join(libDir, 'core/theme'));
moveFolderSync(path.join(libDir, 'config'), path.join(libDir, 'core/config'));

const filesToMove = [
  { from: 'services/auth_service.dart', to: 'features/auth/services/auth_service.dart' },
  { from: 'screens/login_screen.dart', to: 'features/auth/screens/login_screen.dart' },
  { from: 'screens/register_screen.dart', to: 'features/auth/screens/register_screen.dart' },
  { from: 'screens/forgot_password_screen.dart', to: 'features/auth/screens/forgot_password_screen.dart' },
  { from: 'screens/auth/otp_verification_screen.dart', to: 'features/auth/screens/otp_verification_screen.dart' },
  { from: 'screens/auth/reset_password_screen.dart', to: 'features/auth/screens/reset_password_screen.dart' },
  { from: 'screens/patient_dashboard_screen.dart', to: 'features/patient/screens/patient_dashboard_screen.dart' }
];

for (const move of filesToMove) {
  const fromPath = path.join(libDir, move.from);
  const toPath = path.join(libDir, move.to);
  if (fs.existsSync(fromPath)) {
    fs.renameSync(fromPath, toPath);
  }
}

// Clean up old dirs if empty
try { fs.rmdirSync(path.join(libDir, 'screens/auth')); } catch(e){}
try { fs.rmdirSync(path.join(libDir, 'screens')); } catch(e){}
try { fs.rmdirSync(path.join(libDir, 'services')); } catch(e){}

// 3. Update imports globally
const replacements = [
  { regex: /package:sehati_mobile\/theme\//g, replacement: 'package:sehati_mobile/core/theme/' },
  { regex: /package:sehati_mobile\/config\//g, replacement: 'package:sehati_mobile/core/config/' },
  { regex: /package:sehati_mobile\/services\/auth_service\.dart/g, replacement: 'package:sehati_mobile/features/auth/services/auth_service.dart' },
  { regex: /package:sehati_mobile\/screens\/login_screen\.dart/g, replacement: 'package:sehati_mobile/features/auth/screens/login_screen.dart' },
  { regex: /package:sehati_mobile\/screens\/register_screen\.dart/g, replacement: 'package:sehati_mobile/features/auth/screens/register_screen.dart' },
  { regex: /package:sehati_mobile\/screens\/forgot_password_screen\.dart/g, replacement: 'package:sehati_mobile/features/auth/screens/forgot_password_screen.dart' },
  { regex: /package:sehati_mobile\/screens\/auth\//g, replacement: 'package:sehati_mobile/features/auth/screens/' },
  { regex: /package:sehati_mobile\/screens\/patient_dashboard_screen\.dart/g, replacement: 'package:sehati_mobile/features/patient/screens/patient_dashboard_screen.dart' },
  
  // Convert basic relative imports to absolute so they definitely work after move
  { regex: /'\.\.\/\.\.\/services\/auth_service\.dart'/g, replacement: "'package:sehati_mobile/features/auth/services/auth_service.dart'" },
  { regex: /'\.\.\/services\/auth_service\.dart'/g, replacement: "'package:sehati_mobile/features/auth/services/auth_service.dart'" },
  { regex: /'\.\.\/\.\.\/theme\//g, replacement: "'package:sehati_mobile/core/theme/" },
  { regex: /'\.\.\/theme\//g, replacement: "'package:sehati_mobile/core/theme/" },
  { regex: /'\.\.\/config\//g, replacement: "'package:sehati_mobile/core/config/" },
  { regex: /'\.\.\/\.\.\/config\//g, replacement: "'package:sehati_mobile/core/config/" },
];

function processDirectory(directory) {
  const files = fs.readdirSync(directory);
  for (const file of files) {
    const fullPath = path.join(directory, file);
    if (fs.statSync(fullPath).isDirectory()) {
      processDirectory(fullPath);
    } else if (fullPath.endsWith('.dart')) {
      let content = fs.readFileSync(fullPath, 'utf8');
      let original = content;
      for (const rule of replacements) {
        content = content.replace(rule.regex, rule.replacement);
      }
      if (content !== original) {
        fs.writeFileSync(fullPath, content, 'utf8');
        console.log('Updated imports in:', fullPath);
      }
    }
  }
}

processDirectory(libDir);
