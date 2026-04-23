class ApiConfig {
  static const bool isProduction = bool.fromEnvironment('dart.vm.product');

  static const String baseUrl = isProduction
      ? 'https://YOUR_PROD_URL/api'
      //: 'http://[IP_ADDRESS]/api';
      : 'http://192.168.1.94:6060/api';
      
  static const String authUrl = '$baseUrl/auth';
}
