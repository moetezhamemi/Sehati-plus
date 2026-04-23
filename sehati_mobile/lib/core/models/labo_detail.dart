import 'labo_summary.dart';

class DaySchedule {
  final String? debut;
  final String? fin;
  final bool ferme;

  DaySchedule({
    this.debut,
    this.fin,
    required this.ferme,
  });

  factory DaySchedule.fromJson(Map<String, dynamic>? json) {
    if (json == null) {
      return DaySchedule(ferme: true);
    }
    return DaySchedule(
      debut: json['debut'],
      fin: json['fin'],
      ferme: json['ferme'] ?? false,
    );
  }
}

class WorkHours {
  final DaySchedule? lundi;
  final DaySchedule? mardi;
  final DaySchedule? mercredi;
  final DaySchedule? jeudi;
  final DaySchedule? vendredi;
  final DaySchedule? samedi;
  final DaySchedule? dimanche;

  WorkHours({
    this.lundi,
    this.mardi,
    this.mercredi,
    this.jeudi,
    this.vendredi,
    this.samedi,
    this.dimanche,
  });

  factory WorkHours.fromJson(Map<String, dynamic>? json) {
    if (json == null) return WorkHours();
    return WorkHours(
      lundi: DaySchedule.fromJson(json['lundi']),
      mardi: DaySchedule.fromJson(json['mardi']),
      mercredi: DaySchedule.fromJson(json['mercredi']),
      jeudi: DaySchedule.fromJson(json['jeudi']),
      vendredi: DaySchedule.fromJson(json['vendredi']),
      samedi: DaySchedule.fromJson(json['samedi']),
      dimanche: DaySchedule.fromJson(json['dimanche']),
    );
  }
}

class LaboDetail {
  final int id;
  final String nomLabo;
  final String adresseComplete;
  final String ville;
  final String? photoProfilUrl;
  final double averageRating;
  final int reviewCount;
  final String? telephone;
  final String? email;
  final List<String> analyses;
  final String? responsable;
  final double? latitude;
  final double? longitude;
  final WorkHours? workHours;

  LaboDetail({
    required this.id,
    required this.nomLabo,
    required this.adresseComplete,
    required this.ville,
    this.photoProfilUrl,
    required this.averageRating,
    required this.reviewCount,
    this.telephone,
    this.email,
    required this.analyses,
    this.responsable,
    this.latitude,
    this.longitude,
    this.workHours,
  });

  factory LaboDetail.fromJson(Map<String, dynamic> json) {
    return LaboDetail(
      id: json['id'],
      nomLabo: json['nomLabo'] ?? '',
      adresseComplete: json['adresseComplete'] ?? '',
      ville: json['ville'] ?? '',
      photoProfilUrl: json['photoProfilUrl'],
      averageRating: (json['averageRating'] ?? 0.0).toDouble(),
      reviewCount: json['reviewCount'] ?? 0,
      telephone: json['telephone'],
      email: json['email'],
      analyses: List<String>.from(json['analyses'] ?? []),
      responsable: json['responsable'],
      latitude: json['latitude'] != null ? json['latitude'].toDouble() : null,
      longitude: json['longitude'] != null ? json['longitude'].toDouble() : null,
      workHours: WorkHours.fromJson(json['workHours']),
    );
  }
}
