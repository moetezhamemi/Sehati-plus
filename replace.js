const fs = require('fs');
const path = require('path');

const replacements = [
  { regex: /#(8b5cf6|3b82f6|14b8a6|6366F1|6366f1|4f46e5|4F46E5|3B82F6|4f46e5)\b/gi, replacement: 'var(--primary-color)' },
  { regex: /#(4338CA|4338ca|1d4ed8|1D4ED8)\b/gi, replacement: 'var(--primary-hover)' },
  { regex: /#(10B981|10b981|15803d|15803D|22c55e|22C55E)\b/gi, replacement: 'var(--success-color)' },
  { regex: /#(b91c1c|B91C1C|ef4444|EF4444|dc2626|DC2626|f87171|F87171|fecaca|FECACA)\b/gi, replacement: 'var(--error-color)' },
  { regex: /#(1F2937|1f2937|111827|374151)\b/gi, replacement: 'var(--text-dark)' },
  { regex: /#(f4f7f6|F4F7F6|f8fafc|F8FAFC|f3f4f6|F3F4F6|EEF2FF|eef2ff|f0f4ff|fef2f2|FEF2F2)\b/gi, replacement: 'var(--bg-color)' },
  { regex: /#(818CF8|818cf8|60a5fa|60A5FA)\b/gi, replacement: 'var(--secondary-color)' },
  { regex: /#(4b5563|4B5563|6b7280|6B7280|9ca3af|9CA3AF)\b/gi, replacement: 'var(--text-dark)' }, 
  { regex: /#(e2e8f0|E2E8F0|d1d5db|D1D5DB|e5e7eb|E5E7EB)\b/gi, replacement: 'var(--secondary-color)' }
];

function processDirectory(directory) {
  const files = fs.readdirSync(directory);
  for (const file of files) {
    const fullPath = path.join(directory, file);
    if (fs.statSync(fullPath).isDirectory()) {
      processDirectory(fullPath);
    } else if (fullPath.match(/\.(css|html|ts)$/)) {
      let content = fs.readFileSync(fullPath, 'utf8');
      let original = content;
      for (const rule of replacements) {
        content = content.replace(rule.regex, rule.replacement);
      }
      if (content !== original) {
        console.log('Modified:', fullPath);
        fs.writeFileSync(fullPath, content, 'utf8');
      }
    }
  }
}

processDirectory('c:/Sehati+/sehati-front/src/app');
