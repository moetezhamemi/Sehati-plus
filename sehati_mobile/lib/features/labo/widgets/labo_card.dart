import 'package:flutter/material.dart';
import 'package:sehati_mobile/core/models/labo_summary.dart';
import 'package:sehati_mobile/core/theme/app_colors.dart';
import 'package:sehati_mobile/core/widgets/localisation_icon.dart';

class LaboCard extends StatelessWidget {
  final LaboSummary labo;
  final VoidCallback? onTap;

  const LaboCard({super.key, required this.labo, this.onTap});

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(16),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withOpacity(0.05),
              blurRadius: 10,
              offset: const Offset(0, 4),
            ),
          ],
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Image Section
            Stack(
              children: [
                ClipRRect(
                  borderRadius: const BorderRadius.vertical(
                    top: Radius.circular(16),
                  ),
                  child: labo.photoProfilUrl != null
                      ? Image.network(
                          labo.photoProfilUrl!,
                          height: 150,
                          width: double.infinity,
                          fit: BoxFit.cover,
                          errorBuilder: (context, error, stackTrace) =>
                              _buildPlaceholder(),
                        )
                      : _buildPlaceholder(),
                ),
                if (labo.averageRating > 0)
                  Positioned(
                    top: 12,
                    right: 12,
                    child: Container(
                      padding: const EdgeInsets.symmetric(
                        horizontal: 8,
                        vertical: 4,
                      ),
                      decoration: BoxDecoration(
                        color: Colors.white.withOpacity(0.9),
                        borderRadius: BorderRadius.circular(8),
                      ),
                      child: Row(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          const Icon(Icons.star, color: Colors.amber, size: 16),
                          const SizedBox(width: 4),
                          Text(
                            labo.averageRating.toStringAsFixed(1),
                            style: const TextStyle(
                              fontWeight: FontWeight.bold,
                              fontSize: 12,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
              ],
            ),

            // Content Section
            Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    labo.nomLabo,
                    style: const TextStyle(
                      fontSize: 18,
                      fontWeight: FontWeight.bold,
                      color: AppColors.primary,
                    ),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    labo.ville,
                    style: TextStyle(
                      fontSize: 14,
                      color: Colors.blue.shade300,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                  const SizedBox(height: 8),

                  // Rating Bar
                  Row(
                    children: [
                      ...List.generate(5, (index) {
                        return Icon(
                          index < labo.averageRating.floor()
                              ? Icons.star
                              : (index < labo.averageRating
                                    ? Icons.star_half
                                    : Icons.star_border),
                          color: Colors.amber,
                          size: 18,
                        );
                      }),
                      const SizedBox(width: 8),
                      Text(
                        '(${labo.reviewCount}) reviews',
                        style: TextStyle(
                          fontSize: 12,
                          color: Colors.grey.shade600,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 12),

                  // Location
                  Row(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const LocalisationIcon(size: 16),
                      const SizedBox(width: 6),
                      Expanded(
                        child: Text(
                          labo.adresseComplete,
                          style: TextStyle(
                            fontSize: 13,
                            color: Colors.grey.shade600,
                            height: 1.4,
                          ),
                          maxLines: 2,
                          overflow: TextOverflow.ellipsis,
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildPlaceholder() {
    return Container(
      height: 150,
      width: double.infinity,
      color: Colors.grey.shade100,
      child: Icon(Icons.business, size: 50, color: Colors.grey.shade300),
    );
  }
}
