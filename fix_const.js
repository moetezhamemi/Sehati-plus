const fs = require('fs');
const path = require('path');

function processDirectory(directory) {
  const files = fs.readdirSync(directory);
  for (const file of files) {
    const fullPath = path.join(directory, file);
    if (fs.statSync(fullPath).isDirectory()) {
      processDirectory(fullPath);
    } else if (fullPath.endsWith('.dart')) {
      let content = fs.readFileSync(fullPath, 'utf8');
      let original = content;
      
      content = content.replace(/const\s+AppColors/g, 'AppColors');
      
      if (content !== original) {
        console.log('Modified:', fullPath);
        fs.writeFileSync(fullPath, content, 'utf8');
      }
    }
  }
}

processDirectory('c:/Sehati+/sehati_mobile/lib');
