interface Props {
  imageUrl: string | null;
  /** Shown when there is no photo. */
  emoji?: string;
  alt: string;
  className?: string;
}

/** A catalogue photo, or the emoji (or nothing) when none has been uploaded. */
export default function CatalogImage({ imageUrl, emoji, alt, className = "" }: Props) {
  if (imageUrl) {
    return (
      <img
        src={imageUrl}
        alt={alt}
        loading="lazy"
        className={`object-cover ${className}`}
      />
    );
  }
  return (
    <span className={`flex items-center justify-center text-5xl ${className}`} aria-hidden="true">
      {emoji}
    </span>
  );
}
