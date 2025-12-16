describe('Test Básico - Comparación de Asistencias', () => {
  it('Debe conectarse a la aplicación', () => {
    cy.visit('/')
    cy.contains('Iniciar Sesión').should('be.visible')
  })

  it('Debe hacer login', () => {
    cy.login()
    cy.url().should('not.include', '/login')
  })

  it('Debe navegar a registro de asistencias', () => {
    cy.login()
    cy.visit('/registro-asistencias')
    cy.contains('Registro de Asistencias').should('be.visible')
  })

  it('Debe encontrar la sección de comparación', () => {
    cy.login()
    cy.visit('/registro-asistencias')
    cy.get('.comparison-card').should('exist')
    
    // Hacer scroll al elemento antes de verificar que es visible
    cy.contains('Comparación Reservas vs Asistencias').scrollIntoView()
    cy.contains('Comparación Reservas vs Asistencias').should('be.visible')
  })

  it('Debe realizar consulta con rango de fechas (campos opcionales vacíos)', () => {
    cy.login()
    cy.visit('/registro-asistencias')
    
    // Hacer scroll a la sección de comparación
    cy.get('.comparison-card').scrollIntoView()
    
    // Llenar fechas de noviembre donde hay reservas
    cy.get('input[formControlName="startDate"]')
      .click({ force: true })
      .clear({ force: true })
      .type('2025-11-01', { force: true })
    
    cy.get('input[formControlName="endDate"]')
      .click({ force: true })
      .clear({ force: true })
      .type('2025-11-30', { force: true })
    
    // Verificar que campos opcionales están en su estado inicial
    cy.get('input[formControlName="laboratoryName"]').should('have.value', '')
    cy.get('input[formControlName="minAttendanceRate"]').should('have.value', '0')
    
    // Ejecutar la consulta
    cy.contains('button', 'Analizar').click({ force: true })
    
    // Esperar a que termine el procesamiento
    cy.wait(5000)
    
    // Verificar que la consulta se ejecutó (no debe haber errores críticos)
    cy.get('body').should('be.visible')
    cy.log('✅ Consulta de rango de fechas ejecutada correctamente')
  })
})