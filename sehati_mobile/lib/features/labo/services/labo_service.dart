import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:sehati_mobile/core/config/api_config.dart';
import 'package:sehati_mobile/core/models/labo_summary.dart';
import 'package:sehati_mobile/core/models/labo_detail.dart';

class LaboService {
  final String _baseUrl = ApiConfig.baseUrl;

  Future<Map<String, dynamic>> getPublicLabos({int page = 0, int size = 12}) async {
    try {
      final response = await http.get(
        Uri.parse('$_baseUrl/public/labos?page=$page&size=$size'),
        headers: {'Content-Type': 'application/json'},
      );

      if (response.statusCode == 200) {
        final Map<String, dynamic> data = jsonDecode(response.body);
        final List<dynamic> content = data['content'] ?? [];
        
        return {
          'success': true,
          'labos': content.map((json) => LaboSummary.fromJson(json)).toList(),
          'totalElements': data['totalElements'] ?? 0,
          'totalPages': data['totalPages'] ?? 0,
          'isLast': data['last'] ?? true,
        };
      } else {
        return {
          'success': false,
          'message': 'Erreur lors de la récupération des laboratoires (${response.statusCode})',
        };
      }
    } catch (e) {
      return {
        'success': false,
        'message': 'Erreur de connexion : $e',
      };
    }
  }

  Future<Map<String, dynamic>> getLaboDetail(int id) async {
    try {
      final response = await http.get(
        Uri.parse('$_baseUrl/public/labos/$id'),
        headers: {'Content-Type': 'application/json'},
      );

      if (response.statusCode == 200) {
        final Map<String, dynamic> data = jsonDecode(response.body);
        return {
          'success': true,
          'labo': LaboDetail.fromJson(data),
        };
      } else {
        return {
          'success': false,
          'message': 'Erreur lors de la récupération des détails (${response.statusCode})',
        };
      }
    } catch (e) {
      return {
        'success': false,
        'message': 'Erreur de connexion : $e',
      };
    }
  }
}
