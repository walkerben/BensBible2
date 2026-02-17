import SwiftUI

struct SplashScreenView: View {
    @State private var showCross = false
    @State private var showGlow = false
    @State private var showShimmer = false
    @State private var showTitle = false

    var body: some View {
        ZStack {
            // Deep navy gradient background
            LinearGradient(
                colors: [
                    Color(red: 0.05, green: 0.05, blue: 0.15),
                    Color(red: 0.08, green: 0.08, blue: 0.22)
                ],
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()

            VStack(spacing: 24) {
                // Cross assembly
                ZStack {
                    // Glow pulse behind cross
                    Circle()
                        .fill(
                            RadialGradient(
                                colors: [
                                    Color(red: 1.0, green: 0.84, blue: 0.0).opacity(0.3),
                                    Color(red: 1.0, green: 0.75, blue: 0.0).opacity(0.1),
                                    Color.clear
                                ],
                                center: .center,
                                startRadius: 10,
                                endRadius: 80
                            )
                        )
                        .frame(width: 160, height: 160)
                        .scaleEffect(showGlow ? 1.15 : 0.9)
                        .opacity(showGlow ? 0.8 : 0.3)
                        .animation(
                            showGlow
                                ? .easeInOut(duration: 1.5).repeatForever(autoreverses: true)
                                : .default,
                            value: showGlow
                        )

                    // Golden cross
                    Image(systemName: "cross.fill")
                        .font(.system(size: 80, weight: .regular))
                        .foregroundStyle(
                            LinearGradient(
                                colors: [
                                    Color(red: 1.0, green: 0.84, blue: 0.0),  // #FFD700
                                    Color(red: 0.72, green: 0.53, blue: 0.04) // #B8860B
                                ],
                                startPoint: .top,
                                endPoint: .bottom
                            )
                        )
                        .scaleEffect(showCross ? 1.0 : 0.6)
                        .opacity(showCross ? 1 : 0)
                        .overlay {
                            // Shimmer ray masked to cross shape
                            if showShimmer {
                                shimmerRay
                                    .mask(
                                        Image(systemName: "cross.fill")
                                            .font(.system(size: 80, weight: .regular))
                                    )
                            }
                        }
                }

                // Title text
                Text("Ben's Bible")
                    .font(.custom("Georgia", size: 28))
                    .foregroundColor(Color(red: 0.85, green: 0.75, blue: 0.55))
                    .opacity(showTitle ? 1 : 0)
                    .offset(y: showTitle ? 0 : 10)
            }
        }
        .onAppear {
            // 0.2s — Cross appears
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.2) {
                withAnimation(.easeOut(duration: 0.8)) {
                    showCross = true
                }
            }
            // 0.6s — Glow pulse starts
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.6) {
                showGlow = true
            }
            // 1.0s — Shimmer sweep
            DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
                withAnimation(.easeInOut(duration: 1.5)) {
                    showShimmer = true
                }
            }
            // 1.2s — Title fades in
            DispatchQueue.main.asyncAfter(deadline: .now() + 1.2) {
                withAnimation(.easeOut(duration: 0.7)) {
                    showTitle = true
                }
            }
        }
    }

    // Diagonal shimmer ray that sweeps across the cross
    private var shimmerRay: some View {
        Rectangle()
            .fill(
                LinearGradient(
                    colors: [
                        Color.clear,
                        Color.white.opacity(0.3),
                        Color.clear
                    ],
                    startPoint: .leading,
                    endPoint: .trailing
                )
            )
            .frame(width: 30, height: 160)
            .rotationEffect(.degrees(35))
            .offset(x: showShimmer ? 60 : -60)
            .animation(.easeInOut(duration: 1.5), value: showShimmer)
            .blendMode(.overlay)
    }
}

#Preview {
    SplashScreenView()
}
