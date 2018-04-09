; Implementations of square root and sine

    ; Use R31 for function return address
    ; Use R30 as stack pointer
    mov R30 1024

    ; Call subroutine to compute sine
    mov r1 6.0
    call r31 sine
    fout r0


    ; Call subroutine to compute sqrt
    mov r1 2.0
    call r31 sqrt
    fout r0


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



; Square root by Newton's method
; R1 is the input argument
; R0 will contain the result
sqrt:
    ; Push registers this will use onto stack
    sub r30 r30 1
    store r2 r30
    sub r30 r30 1
    store r3 r30
    sub r30 r30 1
    store r4 r30
    sub r30 r30 1
    store r5 r30

    mov r2 r1
    mov r3 1.0
    
sqrt_loop:
    fsub r4 r2 r3
    fcmp r5 r4 0.000001
    bra le r5 sqrt_quit

    fadd r2 r2 r3
    fmul r2 r2 0.5
    fdiv r3 r1 r2
    jmp sqrt_loop

sqrt_quit:
    mov r0 r2     ; return value
    ; Restore registers from stack
    load r5 r30
    add r30 r30 1
    load r4 r30
    add r30 r30 1
    load r3 r30
    add r30 r30 1
    load r2 r30
    add r30 r30 1
    jmp r31         ; return from subroutine





; double sin (double x){
;     int i = 1;
;     double cur = x;
;     double acc = 1;
;     double fact= 1;
;     double pow = x;
;     while (fabs(acc) > .00000001 &&   i < 100){
;         fact *= ((2*i)*(2*i+1));
;         pow *= -1 * x*x; 
;         acc =  pow / fact;
;         cur += acc;
;         i++;
;     }
;     return cur;
; }


; Compute sine by Taylor series
; R1 is the input argument
; R0 will contain the result
sine:
    ; Save clobbered registers on stack
    sub r30 r30 1
    store r2 r30
    sub r30 r30 1
    store r3 r30
    sub r30 r30 1
    store r4 r30
    sub r30 r30 1
    store r5 r30
    sub r30 r30 1
    store r6 r30
    sub r30 r30 1
    store r7 r30
    sub r30 r30 1
    store r8 r30
    sub r30 r30 1
    store r9 r30

    ; Confine input value to between -PI and PI
sine_gt_tau:
    fcmp r7 r1 3.141592653589793
    bra le r7 sine_lt_ntau
    fsub r1 r1 6.283185307179586
    jmp sine_gt_tau

sine_lt_ntau:
    fcmp r7 r1 -3.141592653589793
    bra ge r7 sine_init
    fadd r1 r1 6.283185307179586
    jmp sine_lt_ntau


    ; Set up variables used in loop
sine_init:
    mov r2 1.0              ; r2=i
    mov r3 r1               ; r3=cur
    mov r4 1.0              ; r4=acc
    mov r5 1.0              ; r5=fact
    mov r6 r1               ; r6=pow
    fmul r9 r1 r1    
    fsub r9 0.0 r9          ; r9=-x*x

sine_loop:
    fcmp r7 r2 100.0        ; compare r2 to 100
    bra ge r7 sine_quit
    and r7 r4 0x7fffffff    ; r7=fabs(r4)
    fcmp r7 r7 0.00000001   ; compare r7 to small value
    bra le r7 sine_quit
    
    fmul r7 r2 2.0          ; 2*i
    fadd r8 r7 1.0          ; 2*i + 1
    fmul r7 r7 r8           ; ((2*i)*(2*i+1))
    fmul r5 r5 r7           ; fact *= ((2*i)*(2*i+1))

    fmul r6 r6 r9           ; pow *= -x*x
    fdiv r4 r6 r5           ; acc = pow / fact
    fadd r3 r3 r4           ; cur += acc
    fadd r2 r2 1.0          ; i += 1.0
    jmp sine_loop

sine_quit:
    mov r0 r3               ; return value is cur
    ; Restore clobbered registers from stack
    load r9 r30
    add r30 r30 1
    load r8 r30
    add r30 r30 1
    load r7 r30
    add r30 r30 1
    load r6 r30
    add r30 r30 1
    load r5 r30
    add r30 r30 1
    load r4 r30
    add r30 r30 1
    load r3 r30
    add r30 r30 1
    load r2 r30
    add r30 r30 1
    jmp r31                 ; return from subroutine
