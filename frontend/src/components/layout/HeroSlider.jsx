import { useState, useEffect } from 'react';
import { HiChevronLeft, HiChevronRight } from 'react-icons/hi2';

const slides = [
  {
    title: 'Büyük Yaz İndirimleri',
    subtitle: "Tüm ürünlerde %50'ye varan indirimler",
    bgColor: 'from-primary to-pink-600',
    ctaText: 'Alışverişe Başla',
  },
  {
    title: 'Teknoloji Festivali',
    subtitle: 'Elektronik ürünlerde kaçırılmayacak fırsatlar',
    bgColor: 'from-secondary to-gray-700',
    ctaText: 'Fırsatları Gör',
  },
  {
    title: 'Bedava Kargo Günleri',
    subtitle: '150 TL üzeri siparişlerde kargo bedava',
    bgColor: 'from-success to-emerald-600',
    ctaText: 'Hemen Keşfet',
  },
];

export default function HeroSlider() {
  const [current, setCurrent] = useState(0);

  useEffect(() => {
    const timer = setInterval(() => {
      setCurrent((prev) => (prev + 1) % slides.length);
    }, 5000);
    return () => clearInterval(timer);
  }, []);

  const prev = () => setCurrent((c) => (c - 1 + slides.length) % slides.length);
  const next = () => setCurrent((c) => (c + 1) % slides.length);

  return (
    <div className="relative overflow-hidden rounded-xl">
      <div
        className="flex transition-transform duration-500 ease-out"
        style={{ transform: `translateX(-${current * 100}%)` }}
      >
        {slides.map((slide, idx) => (
          <div
            key={idx}
            className={`w-full shrink-0 bg-gradient-to-r ${slide.bgColor} px-8 md:px-16 py-16 md:py-24`}
          >
            <div className="max-w-xl">
              <h2 className="text-3xl md:text-5xl font-black text-white mb-3">
                {slide.title}
              </h2>
              <p className="text-lg md:text-xl text-white/80 mb-6">
                {slide.subtitle}
              </p>
              <button className="px-8 py-3 bg-white text-secondary font-bold rounded-lg hover:bg-accent-yellow transition-colors cursor-pointer">
                {slide.ctaText}
              </button>
            </div>
          </div>
        ))}
      </div>

      {/* Nav arrows */}
      <button
        onClick={prev}
        className="absolute left-3 top-1/2 -translate-y-1/2 p-2 rounded-full bg-white/20 text-white hover:bg-white/40 transition-colors backdrop-blur-sm cursor-pointer"
      >
        <HiChevronLeft className="h-6 w-6" />
      </button>
      <button
        onClick={next}
        className="absolute right-3 top-1/2 -translate-y-1/2 p-2 rounded-full bg-white/20 text-white hover:bg-white/40 transition-colors backdrop-blur-sm cursor-pointer"
      >
        <HiChevronRight className="h-6 w-6" />
      </button>

      {/* Dots */}
      <div className="absolute bottom-4 left-1/2 -translate-x-1/2 flex gap-2">
        {slides.map((_, idx) => (
          <button
            key={idx}
            onClick={() => setCurrent(idx)}
            className={`h-2 rounded-full transition-all cursor-pointer ${
              idx === current ? 'w-8 bg-white' : 'w-2 bg-white/50'
            }`}
          />
        ))}
      </div>
    </div>
  );
}
