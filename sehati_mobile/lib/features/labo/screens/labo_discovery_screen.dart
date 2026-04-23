import 'package:flutter/material.dart';
import 'package:sehati_mobile/core/models/labo_summary.dart';
import 'package:sehati_mobile/core/theme/app_colors.dart';
import 'package:sehati_mobile/features/labo/services/labo_service.dart';
import 'package:sehati_mobile/features/labo/widgets/labo_card.dart';
import 'package:sehati_mobile/features/labo/screens/labo_detail_screen.dart';

class LaboDiscoveryScreen extends StatefulWidget {
  const LaboDiscoveryScreen({super.key});

  @override
  State<LaboDiscoveryScreen> createState() => _LaboDiscoveryScreenState();
}

class _LaboDiscoveryScreenState extends State<LaboDiscoveryScreen> {
  final LaboService _laboService = LaboService();
  final ScrollController _scrollController = ScrollController();
  
  List<LaboSummary> _labos = [];
  bool _isLoading = false;
  bool _isInitialLoading = true;
  int _currentPage = 0;
  bool _isLastPage = false;
  String? _errorMessage;

  @override
  void initState() {
    super.initState();
    _loadInitialLabos();
    _scrollController.addListener(_onScroll);
  }

  @override
  void dispose() {
    _scrollController.dispose();
    super.dispose();
  }

  Future<void> _loadInitialLabos() async {
    setState(() {
      _isInitialLoading = true;
      _errorMessage = null;
    });
    
    await _fetchPage(0);
    
    setState(() {
      _isInitialLoading = false;
    });
  }

  Future<void> _fetchPage(int page) async {
    if (_isLoading) return;
    
    setState(() {
      _isLoading = true;
    });

    final result = await _laboService.getPublicLabos(page: page);

    if (mounted) {
      setState(() {
        _isLoading = false;
        if (result['success']) {
          if (page == 0) {
            _labos = result['labos'];
          } else {
            _labos.addAll(result['labos']);
          }
          _isLastPage = result['isLast'];
          _currentPage = page;
        } else {
          _errorMessage = result['message'];
        }
      });
    }
  }

  void _onScroll() {
    if (_isLastPage || _isLoading || _isInitialLoading) return;
    
    if (_scrollController.position.pixels >= _scrollController.position.maxScrollExtent - 200) {
      _fetchPage(_currentPage + 1);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(
        title: const Text(
          'Trouvez un laboratoire',
          style: TextStyle(fontWeight: FontWeight.bold),
        ),
        elevation: 0,
        backgroundColor: Colors.white,
        foregroundColor: AppColors.primary,
      ),
      body: _buildBody(),
    );
  }

  Widget _buildBody() {
    if (_isInitialLoading) {
      return const Center(child: CircularProgressIndicator());
    }

    if (_errorMessage != null && _labos.isEmpty) {
      return Center(
        child: Padding(
          padding: const EdgeInsets.all(24.0),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Icon(Icons.error_outline, size: 60, color: Colors.grey),
              const SizedBox(height: 16),
              Text(
                _errorMessage!,
                textAlign: TextAlign.center,
                style: const TextStyle(fontSize: 16),
              ),
              const SizedBox(height: 24),
              ElevatedButton(
                onPressed: _loadInitialLabos,
                child: const Text('Réessayer'),
              ),
            ],
          ),
        ),
      );
    }

    if (_labos.isEmpty) {
      return RefreshIndicator(
        onRefresh: _loadInitialLabos,
        child: ListView(
          physics: const AlwaysScrollableScrollPhysics(),
          children: const [
            SizedBox(height: 100),
            Center(
              child: Column(
                children: [
                  Icon(Icons.search_off, size: 60, color: Colors.grey),
                  SizedBox(height: 16),
                  Text('Aucun laboratoire trouvé'),
                ],
              ),
            ),
          ],
        ),
      );
    }

    return RefreshIndicator(
      onRefresh: _loadInitialLabos,
      child: ListView.builder(
        controller: _scrollController,
        padding: const EdgeInsets.symmetric(vertical: 12),
        itemCount: _labos.length + (_isLastPage ? 0 : 1),
        itemBuilder: (context, index) {
          if (index < _labos.length) {
            return LaboCard(
              labo: _labos[index],
              onTap: () {
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (context) => LaboDetailScreen(laboId: _labos[index].id),
                  ),
                );
              },
            );
          } else {
            return const Padding(
              padding: EdgeInsets.symmetric(vertical: 32.0),
              child: Center(child: CircularProgressIndicator()),
            );
          }
        },
      ),
    );
  }
}
