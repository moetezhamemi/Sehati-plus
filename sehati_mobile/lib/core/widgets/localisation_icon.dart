import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';

class LocalisationIcon extends StatelessWidget {
  final double size;
  final Color? color;

  const LocalisationIcon({
    super.key,
    this.size = 20,
    this.color,
  });

  @override
  Widget build(BuildContext context) {
    return SvgPicture.string(
      '''<svg xmlns="http://www.w3.org/2000/svg" width="$size" height="$size" viewBox="0 0 24 24" fill="none" stroke="${_toHex(color ?? const Color(0xFF9CA3AF))}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="lucide lucide-map-pin"><path d="M20 10c0 4.993-5.539 10.193-7.399 11.799a1 1 0 0 1-1.202 0C9.539 20.193 4 14.993 4 10a8 8 0 0 1 16 0"></path><circle cx="12" cy="10" r="3"></circle></svg>''',
      width: size,
      height: size,
    );
  }

  String _toHex(Color color) {
    return '#${color.value.toRadixString(16).substring(2)}';
  }
}
