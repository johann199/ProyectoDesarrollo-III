describe('Reservas Admin - Agendar Práctica', () => {
  beforeEach(() => {
    cy.login()
    cy.visit('/servicios-practicas')
    cy.contains('Mis Prácticas Reservadas').should('be.visible')
  })

  it('Debe crear una nueva reserva de práctica usando Laboratorio Principal', () => {
    // Generar datos únicos para evitar conflictos
    const timestamp = Date.now()
    const subject = `Práctica Test ${timestamp}`
    
    cy.log('Abriendo formulario de agendar práctica')
    
    // Click en el botón "Agendar Práctica"
    cy.contains('button', 'Agendar Práctica').click({ force: true })
    
    // Esperar a que se abra el diálogo
    cy.get('mat-dialog-content').should('be.visible')
    
    // Llenar asignatura/tema
    cy.get('input[formControlName="subject"]')
      .type(subject, { force: true })
    
    // Llenar laboratorio exactamente como "Laboratorio Principal"
    cy.get('input[formControlName="laboratoryName"]')
      .clear({ force: true })
      .type('Laboratorio Principal', { force: true })
    
    // Seleccionar tipo de práctica
    cy.get('mat-select[formControlName="practiceType"]').click({ force: true })
    cy.get('mat-option').contains('Electrónica').click({ force: true })
    
    // Llenar fecha (mañana)
    const tomorrow = new Date()
    tomorrow.setDate(tomorrow.getDate() + 1)
    const dateString = tomorrow.toISOString().split('T')[0]
    
    cy.get('input[formControlName="date"]')
      .type(dateString, { force: true })
    
    // Llenar hora de inicio
    cy.get('input[formControlName="startTime"]')
      .type('10:00', { force: true })
    
    // Llenar duración en minutos (entre 1 y 60)
    cy.get('input[formControlName="durationMinutes"]')
      .clear({ force: true })
      .type('45', { force: true })
    
    // Llenar número de estudiantes (menor a 10)
    cy.get('input[formControlName="studentCount"]')
      .type('8', { force: true })
    
    // Click en Guardar
    cy.contains('button', 'Guardar').click({ force: true })
    
    // Esperar a que se procese y se cierre el diálogo
    cy.wait(3000)
    
    // Verificar que regresamos a la lista principal
    cy.contains('Mis Prácticas Reservadas').should('be.visible')
    
    // Verificar que la nueva práctica aparece en la lista
    cy.contains(subject).should('be.visible')
    cy.contains('Laboratorio Principal').should('be.visible')
    cy.contains('8 estudiante').should('be.visible')
    
    cy.log('✅ Práctica agendada exitosamente')
  })
})