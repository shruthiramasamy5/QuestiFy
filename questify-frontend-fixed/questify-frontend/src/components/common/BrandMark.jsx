export default function BrandMark({ size = 16, stroke = "#FAF6F1" }) {
  return (
    <svg width={size} height={size} viewBox="0 0 16 16" fill="none" aria-hidden="true">
      <path
        d="M4.5 5.2C4.5 3.2 6 2 8 2C9.9 2 11.4 3.1 11.4 4.9C11.4 6.5 10.3 7.1 9.2 7.8C8.3 8.4 8 8.9 8 9.9"
        stroke={stroke}
        strokeWidth="1.6"
        strokeLinecap="round"
      />
      <circle cx="8" cy="12.6" r="1.1" fill={stroke} />
    </svg>
  );
}
