export function decodeBinaryGrid(base64Data: string, rows: number, cols: number): Grid {
  const bytes = atob(base64Data)
  const buffer = new Uint8Array(bytes.length)
  for (let i = 0; i < bytes.length; i++) {
    buffer[i] = bytes.charCodeAt(i)
  }

  const grid = Array(rows)
    .fill(null)
    .map(() => Array(cols).fill(false))

  let bitIndex = 0
  for (let i = 0; i < rows; i++) {
    for (let j = 0; j < cols; j++) {
      const byteIndex = Math.floor(bitIndex / 8)
      const bitPosition = bitIndex % 8

      grid[i][j] = (buffer[byteIndex] & (1 << bitPosition)) !== 0

      bitIndex++
    }
  }

  return grid
}
