function SectionHeader({title, children}) {
    return (
        <div className="section-header">
            <h2>{title}</h2>
            {children && <div className="section-header__actions">{children}</div>}
        </div>
    )
}

export default SectionHeader
