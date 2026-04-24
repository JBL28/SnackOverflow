import pino from 'pino'
import path from 'path'

const isDev = process.env.NODE_ENV === 'development'
const LOG_DIR = path.join(process.cwd(), 'logs')

function buildTransport() {
  if (isDev) {
    return {
      target: 'pino-pretty',
      options: { colorize: true, translateTime: 'SYS:standard', ignore: 'pid,hostname' },
    }
  }
  return {
    targets: [
      {
        target: 'pino-roll',
        options: {
          file: path.join(LOG_DIR, 'frontend.log'),
          frequency: 'daily',
          mkdir: true,
          limit: { count: 7 },
        },
        level: 'info',
      },
      {
        target: 'pino-roll',
        options: {
          file: path.join(LOG_DIR, 'frontend-error.log'),
          frequency: 'daily',
          mkdir: true,
          limit: { count: 7 },
        },
        level: 'error',
      },
    ],
  }
}

const logger = pino(
  {
    level: isDev ? 'debug' : 'info',
    timestamp: pino.stdTimeFunctions.isoTime,
  },
  pino.transport(buildTransport()),
)

export default logger