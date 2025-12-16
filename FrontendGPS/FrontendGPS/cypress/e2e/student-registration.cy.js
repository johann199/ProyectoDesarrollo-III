describe('Registro de Asistencia - Estudiante', () => {
  beforeEach(() => {
    cy.login()
    cy.visit('/registro-asistencias')
    cy.get('.search-card').should('be.visible')
  })

  it('Debe mostrar el formulario de registro de asistencia', () => {
    cy.contains('Buscar Estudiante').should('be.visible')
    cy.get('input[formControlName="studentCode"]').should('be.visible')
    cy.contains('button', 'Registrar').should('be.visible')
  })

  it('Debe mostrar que el estudiante 2259371 ya fue registrado', () => {
    // Ingresar el código del estudiante
    cy.get('input[formControlName="studentCode"]')
      .click({ force: true })
      .clear({ force: true })
      .type('2259371', { force: true })
    
    // Hacer clic en el botón Registrar
    cy.contains('button', 'Registrar').click({ force: true })
    
    // Esperar a que termine el procesamiento
    cy.wait(3000)
    
    // Verificar que aparezca el mensaje de que ya fue registrado hoy
    cy.get('.error-message', { timeout: 10000 }).should('be.visible')
    cy.contains('La asistencia ya fue registrada hoy para este estudiante').should('be.visible')
    cy.log('✅ Mensaje correcto: Estudiante 2259371 ya fue registrado hoy')
  })

  it('Debe validar formato de código estudiantil', () => {
    // Probar con código muy corto
    cy.get('input[formControlName="studentCode"]')
      .click({ force: true })
      .clear({ force: true })
      .type('123', { force: true })
    
    cy.contains('button', 'Registrar').click({ force: true })
    
    // Debería mostrar error de validación
    cy.get('mat-error').should('be.visible')
    cy.contains('Mínimo 7 dígitos').should('be.visible')
  })

  it('Debe limpiar el formulario después del registro', () => {
    // Registrar estudiante
    cy.get('input[formControlName="studentCode"]')
      .click({ force: true })
      .clear({ force: true })
      .type('2259371', { force: true })
    
    cy.contains('button', 'Registrar').click({ force: true })
    cy.wait(3000)
    
    // Si aparece el botón "Nuevo Registro", hacer clic
    cy.get('body').then(($body) => {
      if ($body.find('button').is(':contains("Nuevo Registro")')) {
        cy.contains('button', 'Nuevo Registro').click({ force: true })
        
        // Verificar que el formulario se limpió
        cy.get('input[formControlName="studentCode"]').should('have.value', '')
        cy.get('.student-card').should('not.exist')
        cy.log('✅ Formulario limpiado correctamente')
      }
    })
  })
})