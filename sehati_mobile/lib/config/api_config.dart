class ApiConfig {
  // In release build (flutter build apk --release), isProduction = true automatically
  static const bool isProduction = bool.fromEnvironment('dart.vm.product');

  static const String baseUrl = isProduction
      ? 'https://YOUR_PROD_URL/api' 
      : 'http://192.168.1.6:6060/api';

  static const String authUrl = '$baseUrl/auth';
}
