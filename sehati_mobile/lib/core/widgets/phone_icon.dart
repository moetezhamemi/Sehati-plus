import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';

class PhoneIcon extends StatelessWidget {
  final double size;
  final Color? color;

  const PhoneIcon({
    super.key,
    this.size = 20,
    this.color,
  });

  @override
  Widget build(BuildContext context) {
    return SvgPicture.string(
      '''<svg xmlns="http://www.w3.org/2000/svg" width="$size" height="$size" viewBox="0 0 24 24" fill="none" stroke="${_toHex(color ?? const Color(0xFF9CA3AF))}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="lucide lucide-phone"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"></path></svg>''',
      width: size,
      height: size,
    );
  }

  String _toHex(Color color) {
    return '#${color.value.toRadixString(16).substring(2)}';
  }
}
