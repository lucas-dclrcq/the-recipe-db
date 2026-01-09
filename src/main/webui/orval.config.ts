import { defineConfig } from 'orval'

export default defineConfig({
  api: {
    input: './src/api/openapi.json',
    output: {
      target: './src/api/client.ts',
      client: 'fetch',
      mode: 'single',
    },
  },
})
