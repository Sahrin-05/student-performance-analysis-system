# ===================================================================
# Multi-stage Dockerfile for SPAS React Frontend
# ===================================================================

# Stage 1: Build the Vite production bundle
FROM node:22-alpine AS builder
WORKDIR /app

# Copy dependency manifests
COPY package*.json ./
RUN npm ci

# Copy source code and build production assets
COPY . .
ARG VITE_API_BASE_URL=/api
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}
RUN npm run build

# Stage 2: Serve production assets via hardened Nginx Alpine
FROM nginx:1.27-alpine
WORKDIR /usr/share/nginx/html

# Remove default Nginx welcome page
RUN rm -rf ./*

# Copy compiled SPA bundle from builder
COPY --from=builder /app/dist .

# Copy production Nginx configuration with SPA routing & reverse proxy
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Port 80 for HTTP ingress
EXPOSE 80

# Health check probing Nginx local server
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:80/ || exit 1

CMD ["nginx", "-g", "daemon off;"]
