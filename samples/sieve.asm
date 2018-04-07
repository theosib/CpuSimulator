
; Sieve of Eratosthenes
; https://en.wikipedia.org/wiki/Sieve_of_Eratosthenes



    mov r10, 100           ; Select maximum value in list of primes



; Initialize list of numbers
    mov r1, 0              ; Load R1 with zero
init_list:
    store r1, r1            ; Store the value of R1 in memory location R1
    add r1, r1, 1           ; Increment R1
    cmp r2, r1, r10         ; Compare R1 to R10, put comparison code in R2
    bra lt, r2, init_list   ; Loop if counter < 100

    ; Mark 1 as not prime
    mov r1, 0              ; Load R1 with zero
    store r1, r1, 1         ; Store 0 in location 1 (R1+1)



; Outer loop:  Pick number whose multiples will be crossed out
    mov r1, 2              ; Start with crossing out multiples of 2
outer_loop:
    ; Skip inner loop if slot selected by R1 is already crossed out
    load r2, r1             ; Load slot indicated by R1 into R2
    cmp r3, r2, 0           ; Compare R2 to zero, putting flags into R3
    bra eq, r3, next_outer  ; If zero, branch to end of outer loop

; Inner loop:  Cross out multiples of R1
    mov r2, r1              ; Copy R1 into R2
    mov r4, 0               ; Put a zero into R4
inner_loop:
    add r2, r2, r1          ; Increment R2 by R1
    cmp r3, r2, r10         ; Compare R2 to R10, putting flags into R3
    bra ge, r3, next_outer  ; If R2 is >= R10, exit inner loop

    store r4, r2            ; Store zero (R4) into memory location in R2
    jmp inner_loop          ; Unconditionally jump to start of inner loop

next_outer:
    add r1, r1, 1           ; Increment slot whose multiples are to be cleared
    cmp r3, r1, r10         ; Compare R1 to R10, putting flags into R3
    bra lt, r3, outer_loop  ; If less than R10, continue outer loop



; Print loop:  Print out all list entries not crossed out
    mov r1, 2              ; Start with 2
    mov r4, 0              ; Put a zero into R4
print_loop:
    load r2, r1             ; Load value in slot indicated by R1
    cmp r3, r2, 0           ; Compare R2 to zero, putting flags into R3
    bra eq, r3, next_print  ; If zero, branch to end of print loop
    
    out r2                  ; Print prime number

next_print:
    add r1, r1, 1           ; Increment to next slot
    cmp r3, r1, r10         ; Compare R1 to R10, putting flags into R3
    bra lt, r3, print_loop  ; If R2 is < R10, continue print loop

    ; All finished!


    ; Due to varying pipeline lengths, HALT could make it to Writeback before
    ; other instructions are finished, so we just need to insert some NOPs.
    ; We won't need this when we have a reorder buffer, because we can execute
    ; HALT when it retires.
    nop
    nop
    nop
    nop
    nop
    nop
    nop
    nop
    nop
    nop
    nop
    halt
