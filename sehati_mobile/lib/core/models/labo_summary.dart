class LaboSummary {
  final int id;
  final String nomLabo;
  final String adresseComplete;
  final String ville;
  final String? photoProfilUrl;
  final double averageRating;
  final int reviewCount;

  LaboSummary({
    required this.id,
    required this.nomLabo,
    required this.adresseComplete,
    required this.ville,
    this.photoProfilUrl,
    required this.averageRating,
    required this.reviewCount,
  });

  factory LaboSummary.fromJson(Map<String, dynamic> json) {
    return LaboSummary(
      id: json['id'],
      nomLabo: json['nomLabo'] ?? '',
      adresseComplete: json['adresseComplete'] ?? '',
      ville: json['ville'] ?? '',
      photoProfilUrl: json['photoProfilUrl'],
      averageRating: (json['averageRating'] ?? 0.0).toDouble(),
      reviewCount: json['reviewCount'] ?? 0,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'nomLabo': nomLabo,
      'adresseComplete': adresseComplete,
      'ville': ville,
      'photoProfilUrl': photoProfilUrl,
      'averageRating': averageRating,
      'reviewCount': reviewCount,
    };
  }
}
