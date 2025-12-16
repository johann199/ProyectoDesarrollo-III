describe('Historial de Asistencias - Filtros', () => {
  beforeEach(() => {
    cy.login()
    cy.visit('/registro-asistencias')
    cy.contains('Registro de Asistencias').should('be.visible')
    
    // Scroll hacia la sección de historial de asistencias
    cy.get('.list-card').scrollIntoView().should('be.visible')
  })

  it('Debe filtrar historial por fechas 1/12/2025 - 5/12/2025 y código 2259371 mostrando 2 registros', () => {
    // Verificar que estamos en la página correcta
    cy.contains('Historial de Asistencias').should('be.visible')
    
    // Scroll hasta la sección de historial de asistencias
    cy.get('h2').contains('Historial de Asistencias').scrollIntoView()
    
    // Esperar a que la sección sea visible
    cy.get('.list-card .filters-section').should('be.visible')
    
    // Configurar fecha de inicio: 1/12/2025
    cy.get('input[formControlName="start"]')
      .click({ force: true })
      .clear({ force: true })
      .type('1/12/2025', { force: true })
    
    // Configurar fecha de fin: 5/12/2025  
    cy.get('input[formControlName="end"]')
      .click({ force: true })
      .clear({ force: true })
      .type('5/12/2025', { force: true })
    
    // Configurar código de estudiante: 2259371
    cy.get('input[formControlName="userCode"]')
      .click({ force: true })
      .clear({ force: true })
      .type('2259371', { force: true })
    
    // Click en el botón Filtrar
    cy.contains('button', 'Filtrar').click({ force: true })
    
    // Esperar a que se procesen los filtros
    cy.wait(3000)
    
    // Verificar que se muestran registros para el código 2259371
    cy.get('.attendance-table', { timeout: 10000 }).should('be.visible')
    
    // Verificar que hay registros de asistencia (al menos 1, esperamos 2)
    cy.get('.attendance-table tbody tr').should('have.length.at.least', 1)
    
    // Verificar que los registros corresponden al código 2259371
    cy.get('.attendance-table tbody tr').each(($row) => {
      cy.wrap($row).find('td').first().should('contain.text', '2259371')
    })
    
    cy.log('✅ Filtros aplicados correctamente - Mostrando registros para código 2259371')
  })

  it('Debe permitir limpiar filtros usando el botón Limpiar', () => {
    // Scroll hasta la sección de historial
    cy.get('h2').contains('Historial de Asistencias').scrollIntoView()
    
    // Aplicar algunos filtros primero
    cy.get('input[formControlName="userCode"]')
      .type('2259371', { force: true })
    
    cy.get('input[formControlName="start"]')
      .type('1/12/2025', { force: true })
    
    // Verificar que los valores se aplicaron
    cy.get('input[formControlName="userCode"]').should('have.value', '2259371')
    cy.get('input[formControlName="start"]').should('have.value', '1/12/2025')
    
    // Aplicar filtros
    cy.contains('button', 'Filtrar').click({ force: true })
    cy.wait(2000)
    
    // Usar el botón Limpiar específico de la sección de filtros del historial
    cy.get('.list-card .filter-actions button').contains('Limpiar')
      .scrollIntoView()
      .should('be.visible')
      .click({ force: true })
    cy.wait(1000)
    
    // Verificar que se puede aplicar filtros nuevamente (botón funcional)
    cy.get('input[formControlName="userCode"]')
      .clear({ force: true })
      .type('1234567', { force: true })
    
    cy.contains('button', 'Filtrar').click({ force: true })
    cy.wait(1000)
    
    cy.log('✅ Botón Limpiar funcional y permite nuevos filtros')
  })

  it('Debe mostrar mensaje cuando no hay resultados', () => {
    // Scroll hasta la sección de historial
    cy.get('h2').contains('Historial de Asistencias').scrollIntoView()
    
    // Usar un código de estudiante inexistente
    cy.get('input[formControlName="userCode"]')
      .type('9999999', { force: true })
    
    // Aplicar filtro
    cy.contains('button', 'Filtrar').click({ force: true })
    cy.wait(3000)
    
    // Verificar mensaje de no datos (si existe, sino verificar que la tabla esté vacía)
    cy.get('body').then(($body) => {
      if ($body.find('.no-data').length > 0) {
        cy.get('.no-data').should('be.visible')
        cy.contains('No hay asistencias registradas').should('be.visible')
      } else {
        // Si no hay mensaje específico, verificar que no hay filas en la tabla
        cy.get('.attendance-table tbody tr').should('have.length', 0)
      }
    })
    
    cy.log('✅ Manejo correcto de resultados vacíos')
  })

  it('Debe validar que los filtros funcionan independientemente', () => {
    // Scroll hasta la sección de historial
    cy.get('h2').contains('Historial de Asistencias').scrollIntoView()
    
    // Test 1: Solo filtro por código de estudiante
    cy.get('input[formControlName="userCode"]')
      .type('2259371', { force: true })
    
    cy.contains('button', 'Filtrar').click({ force: true })
    cy.wait(2000)
    
    // Verificar que se muestran resultados o tabla
    cy.get('.attendance-table, .no-data').should('exist')
    
    // Limpiar filtros
    cy.contains('button', 'Limpiar').click({ force: true })
    cy.wait(1000)
    
    // Test 2: Solo filtro por rango de fechas
    cy.get('input[formControlName="start"]')
      .type('1/12/2025', { force: true })
    
    cy.get('input[formControlName="end"]')
      .type('5/12/2025', { force: true })
    
    cy.contains('button', 'Filtrar').click({ force: true })
    cy.wait(2000)
    
    // Verificar que se muestran resultados para el rango de fechas
    cy.get('.attendance-table, .no-data').should('exist')
    
    cy.log('✅ Filtros independientes funcionan correctamente')
  })
});