# Multi-stage build for React frontend
FROM node:20-alpine AS builder

WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci

COPY . .

# Accept build arguments for environment variables
ARG VITE_AUTH0_AUDIENCE=https://numbers-dont-lie-api
ARG VITE_DEMO_MODE=false

ENV VITE_AUTH0_AUDIENCE=${VITE_AUTH0_AUDIENCE}
ENV VITE_DEMO_MODE=${VITE_DEMO_MODE}

# Debug: Print what we received
RUN echo "Building with VITE_DEMO_MODE=${VITE_DEMO_MODE}" && \
    echo "Building with VITE_AUTH0_AUDIENCE=${VITE_AUTH0_AUDIENCE}"

# Build the frontend
RUN npm run build

# Runtime stage - serve with nginx
FROM nginx:alpine

# Copy built app
COPY --from=builder /app/dist /usr/share/nginx/html

# Copy nginx config
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
