function ItemVisual({item, className = ''}) {
    const styles = item.imageUrl
        ? {}
        : {
            background: `linear-gradient(135deg, ${item.placeholderColor || '#FB8500'}, #F3D5B5)`,
        }

    return (
        <div className={`item-visual ${className}`.trim()} style={styles}>
            {item.imageUrl ? (
                <img src={item.imageUrl} alt={item.title}/>
            ) : (
                <span>{item.title}</span>
            )}
        </div>
    )
}

export default ItemVisual
