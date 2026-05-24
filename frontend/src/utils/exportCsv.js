function escapeCsv(value) {
  const stringValue = String(value ?? '')
  return `"${stringValue.replaceAll('"', '""')}"`
}

export function exportItemsToCsv(items) {
  const headers = ['Название', 'Коллекция', 'Статус', 'Цена', 'Краткое описание']
  const rows = items.map((item) => [item.title, item.collectionTitle, item.statusLabel, item.price, item.description])
  const csv = [headers, ...rows].map((row) => row.map(escapeCsv).join(';')).join('\n')
  const blob = new Blob([`\uFEFF${csv}`], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = 'collide-items.csv'
  link.click()
  URL.revokeObjectURL(url)
}
