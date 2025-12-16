const { defineConfig } = require('cypress')

module.exports = defineConfig({
  e2e: {
    baseUrl: "http://localhost:4200",
    viewportWidth: 1280,
    viewportHeight: 720,
    supportFile: "cypress/support/e2e.js",
    specPattern: "cypress/e2e/**/*.cy.{js,jsx,ts,tsx}",
    video: true,
    screenshotOnRunFailure: true,
    defaultCommandTimeout: 15000,
    requestTimeout: 15000,
    responseTimeout: 15000,
    // Configuraciones adicionales para manejar elementos ocultos
    scrollBehavior: 'center',
    animationDistanceThreshold: 20,
    waitForAnimations: true,
    setupNodeEvents(on, config) {
      // implement node event listeners here
    }
  },
})