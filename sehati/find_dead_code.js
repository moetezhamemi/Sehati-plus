const fs = require('fs');
const path = require('path');

function getFiles(dir, files = []) {
  const list = fs.readdirSync(dir);
  for (const file of list) {
    const fullPath = path.join(dir, file);
    const stat = fs.statSync(fullPath);
    if (stat.isDirectory()) {
      getFiles(fullPath, files);
    } else if (fullPath.endsWith('.java')) {
      files.push(fullPath);
    }
  }
  return files;
}

const allJavaFiles = getFiles('src/main/java');
const fileContents = allJavaFiles.map(f => ({ path: f, content: fs.readFileSync(f, 'utf8') }));

for (const javaFile of allJavaFiles) {
  const fileName = path.basename(javaFile, '.java');
  
  if (fileName.endsWith('Controller') || 
      fileName.endsWith('Application') || 
      fileName.endsWith('Exception') || 
      fileName.endsWith('Handler') || 
      fileName.endsWith('Config') || 
      fileName.endsWith('Filter')) {
    continue;
  }

  let usageCount = 0;
  for (const { path: otherPath, content } of fileContents) {
    if (path.resolve(otherPath) === path.resolve(javaFile)) continue;
    
    // Look for exact matches of the class name as an import or type usage
    // e.g. "import ...fileName;" or " fileName " or "<fileName>"
    const regex = new RegExp(`\\b${fileName}\\b`, 'g');
    if (regex.test(content)) {
      usageCount++;
    }
  }

  if (usageCount === 0) {
    console.log(`POTENTIAL DEAD CODE: ${javaFile}`);
  }
}
