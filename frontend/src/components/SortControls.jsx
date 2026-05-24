function SortControls({value, onChange, options, label = 'Сортировка'}) {
    return (
        <label className="feed-control">
            <span>{label}</span>
            <select value={value} onChange={(event) => onChange(event.target.value)}>
                {options.map((option) => (
                    <option key={option.value} value={option.value}>{option.label}</option>
                ))}
            </select>
        </label>
    )
}

export default SortControls
