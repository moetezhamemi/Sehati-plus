const fs = require('fs');
const path = require('path');

const replacements = [
  // Primary
  { regex: /Color\(0xFF818CF8\)/g, replacement: 'AppColors.primary' },
  { regex: /Color\(0xFF6366F1\)/g, replacement: 'AppColors.primary' },
  { regex: /Color\(0xFF3B82F6\)/g, replacement: 'AppColors.primary' },
  
  // Background
  { regex: /Color\(0xFFEEF2FF\)/g, replacement: 'AppColors.background' },
  { regex: /Color\(0xFFF4F7F6\)/g, replacement: 'AppColors.background' },
  { regex: /Color\(0xFFFAFBFC\)/g, replacement: 'AppColors.background' },
  
  // Text Dark
  { regex: /Color\(0xFF111827\)/g, replacement: 'AppColors.textDark' },
  { regex: /Color\(0xFF374151\)/g, replacement: 'AppColors.textDark' },
  { regex: /Color\(0xFF1E2A38\)/g, replacement: 'AppColors.textDark' },
  { regex: /Colors\.black87/g, replacement: 'AppColors.textDark' },

  // Error
  { regex: /Colors\.red\.shade700/g, replacement: 'AppColors.error' },
  { regex: /Colors\.red/g, replacement: 'AppColors.error' },
  
  // Secondary (Hints, borders, light accents)
  { regex: /Color\(0xFFD1D5DB\)/g, replacement: 'AppColors.secondary' },
  { regex: /Color\(0xFFE5E7EB\)/g, replacement: 'AppColors.secondary' },
  { regex: /Color\(0xFF9CA3AF\)/g, replacement: 'AppColors.secondary' },

  // Darker hints to TextDark
  { regex: /Color\(0xFF6B7280\)/g, replacement: 'AppColors.textDark' },
  { regex: /Color\(0xFF4B5563\)/g, replacement: 'AppColors.textDark' },
];

function processDirectory(directory) {
  const files = fs.readdirSync(directory);
  for (const file of files) {
    const fullPath = path.join(directory, file);
    if (fs.statSync(fullPath).isDirectory() && !fullPath.endsWith('theme')) {
      processDirectory(fullPath);
    } else if (fullPath.endsWith('.dart') && !fullPath.endsWith('app_colors.dart')) {
      let content = fs.readFileSync(fullPath, 'utf8');
      let original = content;
      for (const rule of replacements) {
        content = content.replace(rule.regex, rule.replacement);
      }
      
      // Also inject the import if AppColors is used and not imported
      if (content !== original) {
        if (content.includes('AppColors.') && !content.includes('app_colors.dart')) {
            // Find the last import using regex to place our import right after it
            const importMatch = content.match(/import 'package:.+';\n/g);
            if (importMatch && importMatch.length > 0) {
              const lastImport = importMatch[importMatch.length - 1];
              content = content.replace(lastImport, lastImport + "import 'package:sehati_mobile/theme/app_colors.dart';\n");
            } else {
              content = "import 'package:sehati_mobile/theme/app_colors.dart';\n" + content;
            }
        }
        
        console.log('Modified:', fullPath);
        fs.writeFileSync(fullPath, content, 'utf8');
      }
    }
  }
}

// Ensure the directory exists
const themeDir = 'c:/Sehati+/sehati_mobile/lib/theme';
if (!fs.existsSync(themeDir)) {
    fs.mkdirSync(themeDir);
}

processDirectory('c:/Sehati+/sehati_mobile/lib');
