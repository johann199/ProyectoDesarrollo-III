// ***********************************************
// This example commands.js shows you how to
// create various custom commands and overwrite
// existing commands.
//
// For more comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands
// ***********************************************

// Login command for reuse across tests
Cypress.Commands.add('login', (userCode = 'admin_code', password = 'admin_password') => {
  cy.session([userCode, password], () => {
    cy.visit('/login')
    
    // Esperar a que la página cargue completamente
    cy.contains('Iniciar Sesión').should('be.visible')
    
    // Llenar el formulario con los selectores correctos
    cy.get('input[formControlName="username"]', { timeout: 10000 }).should('be.visible').clear().type(userCode)
    cy.get('input[formControlName="password"]').should('be.visible').clear().type(password)
    
    // Hacer clic en el botón de submit
    cy.get('button[type="submit"]').should('be.enabled').click()
    
    // Esperar a que el login sea exitoso (ajustar URL según tu app)
    cy.url({ timeout: 15000 }).should('not.include', '/login')
    
    // Opcional: Verificar que se guardó algún token o dato de autenticación
    // cy.window().its('localStorage').invoke('getItem', 'authToken').should('exist')
  }, {
    validate() {
      // Validar que la sesión sigue activa
      cy.visit('/dashboard')
      cy.url().should('not.include', '/login')
    }
  })
})

// Navigate to attendance page
Cypress.Commands.add('goToAttendance', () => {
  cy.visit('/registro-asistencias')
  cy.contains('Registro de Asistencias').should('be.visible')
})

// Fill comparison form (solo campos requeridos por defecto)
Cypress.Commands.add('fillComparisonForm', (startDate, endDate, lab = null) => {
  // Asegurar que estemos en la sección correcta
  cy.get('.comparison-form').scrollIntoView().should('be.visible')
  
  // Llenar solo campos de fecha (requeridos)
  if (startDate) {
    cy.get('.comparison-form')
      .find('input[formControlName="startDate"]')
      .first()
      .scrollIntoView()
      .wait(500)
      .then($input => {
        if ($input.is(':visible')) {
          cy.wrap($input).click({ force: true }).clear({ force: true }).type(startDate, { force: true })
        } else {
          cy.get('input[formControlName="startDate"]')
            .scrollIntoView()
            .click({ force: true })
            .clear({ force: true })
            .type(startDate, { force: true })
        }
      })
  }
  
  if (endDate) {
    cy.get('.comparison-form')
      .find('input[formControlName="endDate"]')
      .first()
      .scrollIntoView()
      .wait(500)
      .then($input => {
        if ($input.is(':visible')) {
          cy.wrap($input).click({ force: true }).clear({ force: true }).type(endDate, { force: true })
        } else {
          cy.get('input[formControlName="endDate"]')
            .scrollIntoView()
            .click({ force: true })
            .clear({ force: true })
            .type(endDate, { force: true })
        }
      })
  }
  
  // Solo llenar laboratorio si se proporciona explícitamente
  if (lab !== null && lab !== '') {
    cy.get('.comparison-form input[formControlName="laboratoryName"]')
      .scrollIntoView()
      .should('be.visible')
      .clear({ force: true })
      .type(lab, { force: true })
  }
})

// Wait for loading to complete
Cypress.Commands.add('waitForLoading', () => {
  cy.get('mat-progress-spinner').should('not.exist')
})

// Safe click with scroll
Cypress.Commands.add('safeClick', { prevSubject: 'element' }, (subject, options = {}) => {
  cy.wrap(subject)
    .scrollIntoView()
    .should('be.visible')
    .click({ force: true, ...options })
})

// Safe type with scroll
Cypress.Commands.add('safeType', { prevSubject: 'element' }, (subject, text, options = {}) => {
  cy.wrap(subject)
    .scrollIntoView()
    .should('be.visible')
    .clear({ force: true })
    .type(text, { force: true, ...options })
})

// Handle Material Design datepicker
Cypress.Commands.add('fillMatDatepicker', (selector, date) => {
  cy.get(selector)
    .scrollIntoView()
    .wait(500)
    .then($input => {
      if ($input.is(':visible')) {
        // Método 1: Llenar directamente el input
        cy.wrap($input)
          .click({ force: true })
          .clear({ force: true })
          .type(date, { force: true })
      } else {
        // Método 2: Usar force en caso de elementos ocultos
        cy.wrap($input)
          .invoke('val', date)
          .trigger('input')
          .trigger('change')
      }
    })
})

// Enhanced comparison form filler for Material Design (solo campos requeridos)
Cypress.Commands.add('fillComparisonFormEnhanced', (startDate, endDate, options = {}) => {
  // Scroll to comparison section first
  cy.get('.comparison-card').scrollIntoView().should('be.visible')
  
  // Fill start date with multiple strategies
  if (startDate) {
    cy.fillMatDatepicker('.comparison-card input[formControlName="startDate"]', startDate)
  }
  
  // Fill end date
  if (endDate) {
    cy.fillMatDatepicker('.comparison-card input[formControlName="endDate"]', endDate)
  }
  
  // Fill optional fields only if explicitly provided
  if (options.lab) {
    cy.get('.comparison-card input[formControlName="laboratoryName"]')
      .scrollIntoView()
      .should('be.visible')
      .clear({ force: true })
      .type(options.lab, { force: true })
  }
  
  if (options.minRate) {
    cy.get('.comparison-card input[formControlName="minAttendanceRate"]')
      .scrollIntoView()
      .should('be.visible')
      .clear({ force: true })
      .type(options.minRate.toString(), { force: true })
  }
})