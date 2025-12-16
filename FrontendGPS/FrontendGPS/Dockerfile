FROM node:24

WORKDIR /app

# Copiar archivos de dependencias
COPY package*.json ./

# Instalar dependencias
RUN npm install --legacy-peer-deps

# Copiar todo el código fuente
COPY . .

# Exponer puerto de desarrollo de Angular
EXPOSE 4200

# Comando para iniciar servidor de desarrollo
CMD ["npm", "start", "--", "--host", "0.0.0.0", "--poll", "1000"]