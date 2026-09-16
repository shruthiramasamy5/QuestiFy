# ==============================================================================
# Stage 1: Build React + Vite SPA
# ==============================================================================
FROM node:20-alpine AS build

WORKDIR /app

# Install dependencies
COPY package*.json ./
RUN npm install

# Copy source code and build production bundle
COPY . .
ARG VITE_API_BASE_URL=""
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
RUN npm run build

# ==============================================================================
# Stage 2: Serve via Nginx with Dynamic Port & Gateway Proxy
# ==============================================================================
FROM nginx:alpine

# Remove default Nginx welcome page
RUN rm -rf /usr/share/nginx/html/*

# Copy built SPA bundle from build stage
COPY --from=build /app/dist /usr/share/nginx/html

# Copy template for dynamic environment variable substitution on startup
COPY nginx.conf.template /etc/nginx/templates/default.conf.template
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Default environment variables (overridden by Render or Docker Compose)
ENV PORT=80
ENV GATEWAY_URL=http://questify-api-gateway:8000

EXPOSE 80 10000

CMD ["nginx", "-g", "daemon off;"]
