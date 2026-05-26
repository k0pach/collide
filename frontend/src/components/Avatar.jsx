function Avatar({ tone = 'orange', size = 'md', label = 'C' }) {
  const initials = label
    .split(' ')
    .map((word) => word[0])
    .join('')
    .slice(0, 2)
    .toUpperCase()

  return <div className={`avatar avatar--${tone} avatar--${size}`}>{initials}</div>
}

export default Avatar
