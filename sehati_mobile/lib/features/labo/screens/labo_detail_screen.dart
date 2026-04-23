import 'package:flutter/material.dart';
import 'package:sehati_mobile/core/models/labo_detail.dart';
import 'package:sehati_mobile/core/theme/app_colors.dart';
import 'package:sehati_mobile/features/labo/services/labo_service.dart';
import 'package:sehati_mobile/core/widgets/localisation_icon.dart';
import 'package:sehati_mobile/core/widgets/phone_icon.dart';

class LaboDetailScreen extends StatefulWidget {
  final int laboId;

  const LaboDetailScreen({super.key, required this.laboId});

  @override
  State<LaboDetailScreen> createState() => _LaboDetailScreenState();
}

class _LaboDetailScreenState extends State<LaboDetailScreen> {
  final LaboService _laboService = LaboService();
  LaboDetail? _labo;
  bool _isLoading = true;
  String? _errorMessage;

  @override
  void initState() {
    super.initState();
    _fetchDetail();
  }

  Future<void> _fetchDetail() async {
    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    final result = await _laboService.getLaboDetail(widget.laboId);
    
    if (mounted) {
      if (result['success']) {
        setState(() {
          _labo = result['labo'];
          _isLoading = false;
        });
      } else {
        setState(() {
          _errorMessage = result['message'];
          _isLoading = false;
        });
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(
        title: const Text('Détails du laboratoire', style: TextStyle(fontWeight: FontWeight.bold)),
        backgroundColor: Colors.white,
        foregroundColor: AppColors.primary,
        elevation: 0,
      ),
      body: _buildBody(),
    );
  }

  Widget _buildBody() {
    if (_isLoading) {
      return const Center(child: CircularProgressIndicator());
    }

    if (_errorMessage != null) {
      return Center(
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Icon(Icons.error_outline, color: Colors.grey, size: 60),
              const SizedBox(height: 16),
              Text(_errorMessage!, textAlign: TextAlign.center),
              const SizedBox(height: 16),
              ElevatedButton(onPressed: _fetchDetail, child: const Text('Réessayer'))
            ],
          ),
        ),
      );
    }

    if (_labo == null) return const SizedBox.shrink();

    return SingleChildScrollView(
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          children: [
            _buildMainCard(),
            const SizedBox(height: 16),
            _buildScheduleCard(),
          ],
        ),
      ),
    );
  }

  Widget _buildMainCard() {
    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        boxShadow: [
          BoxShadow(color: Colors.black.withOpacity(0.05), blurRadius: 10, offset: const Offset(0, 4))
        ]
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          ClipRRect(
            borderRadius: const BorderRadius.vertical(top: Radius.circular(16)),
            child: _labo!.photoProfilUrl != null 
                ? Image.network(_labo!.photoProfilUrl!, height: 160, fit: BoxFit.cover, errorBuilder: (_,__,___) => _buildPlaceholder())
                : _buildPlaceholder()
          ),
          Padding(
            padding: const EdgeInsets.all(16.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  _labo!.nomLabo,
                  style: const TextStyle(fontSize: 22, fontWeight: FontWeight.bold, color: AppColors.primary),
                ),
                if (_labo!.responsable != null && _labo!.responsable!.isNotEmpty)
                  Padding(
                    padding: const EdgeInsets.only(top: 4),
                    child: Text('Responsable: ${_labo!.responsable}', style: TextStyle(color: Colors.grey.shade700)),
                  ),
                const SizedBox(height: 12),
                Row(
                  children: [
                    ...List.generate(5, (index) => Icon(
                      index < _labo!.averageRating.floor() ? Icons.star : (index < _labo!.averageRating ? Icons.star_half : Icons.star_border),
                      color: Colors.amber, size: 20
                    )),
                    const SizedBox(width: 8),
                    Text('${_labo!.averageRating.toStringAsFixed(1)} (${_labo!.reviewCount} avis)', style: const TextStyle(fontWeight: FontWeight.w600)),
                  ],
                ),
                const SizedBox(height: 16),
                _buildActionButtons(),
                const Divider(height: 32),
                _buildInfoRow(Icons.location_on, '${_labo!.adresseComplete}, ${_labo!.ville}'),
                const SizedBox(height: 12),
                _buildInfoRow(Icons.phone, _labo!.telephone ?? 'Non renseigné'),
                if (_labo!.email != null) ...[
                  const SizedBox(height: 12),
                  _buildInfoRow(Icons.email, _labo!.email!),
                ],
                if (_labo!.analyses.isNotEmpty) ...[
                  const Divider(height: 32),
                  const Text('Analyses disponibles', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: AppColors.primary)),
                  const SizedBox(height: 12),
                  Wrap(
                    spacing: 8,
                    runSpacing: 8,
                    children: _labo!.analyses.map((analyse) => Container(
                      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                      decoration: BoxDecoration(
                        color: Colors.white,
                        borderRadius: BorderRadius.circular(30),
                        border: Border.all(color: Colors.grey.shade200)
                      ),
                      child: Text(
                        analyse, 
                        style: const TextStyle(
                          color: AppColors.textDark,
                          fontSize: 13, 
                          fontWeight: FontWeight.w600
                        )
                      ),
                    )).toList()
                  ),
                ]
              ],
            ),
          )
        ],
      )
    );
  }

  Widget _buildActionButtons() {
    return Column(
      children: [
        SizedBox(
          width: double.infinity,
          child: ElevatedButton.icon(
            onPressed: () {},
            icon: const Icon(Icons.calendar_today),
            label: const Text('Prendre rendez-vous'),
            style: ElevatedButton.styleFrom(
              padding: const EdgeInsets.symmetric(vertical: 12),
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
            )
          ),
        ),
        const SizedBox(height: 8),
        Row(
          children: [
            Expanded(
              child: OutlinedButton.icon(
                onPressed: () {},
                icon: const Icon(Icons.near_me, size: 18),
                label: const Text('Itinéraire'),
                style: OutlinedButton.styleFrom(padding: const EdgeInsets.symmetric(vertical: 12))
              ),
            ),
            const SizedBox(width: 8),
            Expanded(
              child: OutlinedButton.icon(
                onPressed: () {},
                icon: const Icon(Icons.phone, size: 18),
                label: const Text('WhatsApp'),
                style: OutlinedButton.styleFrom(padding: const EdgeInsets.symmetric(vertical: 12))
              ),
            ),
          ],
        )
      ],
    );
  }

  Widget _buildInfoRow(IconData icon, String text) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        if (icon == Icons.location_on)
          const LocalisationIcon(size: 20)
        else if (icon == Icons.phone)
          const PhoneIcon(size: 20)
        else
          Icon(icon, color: Colors.grey.shade500, size: 20),
        const SizedBox(width: 12),
        Expanded(child: Text(text, style: const TextStyle(fontSize: 14, height: 1.4))),
      ],
    );
  }

  Widget _buildScheduleCard() {
    if (_labo!.workHours == null) return const SizedBox.shrink();
    
    final days = [
      {'label': 'Lundi', 'schedule': _labo!.workHours!.lundi},
      {'label': 'Mardi', 'schedule': _labo!.workHours!.mardi},
      {'label': 'Mercredi', 'schedule': _labo!.workHours!.mercredi},
      {'label': 'Jeudi', 'schedule': _labo!.workHours!.jeudi},
      {'label': 'Vendredi', 'schedule': _labo!.workHours!.vendredi},
      {'label': 'Samedi', 'schedule': _labo!.workHours!.samedi},
      {'label': 'Dimanche', 'schedule': _labo!.workHours!.dimanche},
    ];

    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        boxShadow: [
          BoxShadow(color: Colors.black.withOpacity(0.05), blurRadius: 10, offset: const Offset(0, 4))
        ]
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text('Horaires d\'ouverture', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: AppColors.primary)),
          const SizedBox(height: 16),
          ...days.map((dy) {
            final day = dy['schedule'] as DaySchedule?;
            final bool isClosed = day == null || day.ferme;
            return Padding(
              padding: const EdgeInsets.symmetric(vertical: 6),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text(dy['label'] as String, style: const TextStyle(fontWeight: FontWeight.w500)),
                  isClosed
                    ? const Text('Fermé', style: TextStyle(color: Colors.red, fontWeight: FontWeight.bold))
                    : Text('${(day.debut ?? '').substring(0,5)} - ${(day.fin ?? '').substring(0,5)}')
                ],
              ),
            );
          }),
        ],
      ),
    );
  }

  Widget _buildPlaceholder() {
    return Container(
      height: 160,
      width: double.infinity,
      color: Colors.grey.shade100,
      child: Icon(Icons.business, size: 60, color: Colors.grey.shade300),
    );
  }
}
