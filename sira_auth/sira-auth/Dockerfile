
FROM node:24 AS build
WORKDIR /app

COPY package*.json ./
RUN npm install --production=false

COPY . .
RUN npm run build


FROM node:24
WORKDIR /app

COPY package*.json ./
RUN npm install --production

COPY --from=build /app/dist ./dist

EXPOSE 3000

CMD ["node", "dist/app.js"]
