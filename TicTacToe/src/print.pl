% When every square has been printed, finish it
% with a line.
print_list([]).
print_list([H|T]) :-
    print_board(H),
    print_list(T).

print_board(_/[]) :- print_line.

% Print the items, three at a time.
print_board(_/[S1, S2, S3 | Rest]) :-
    print_line,
    print_item(S1),
    print_item(S2),
    print_item(S3),
    write('| \n'),
    print_board(Rest).

% The size of the board is fixed, so lines are
% easy.
print_line :-
    write_ln('+---+---+---+').

% Print the item depending on 
print_item(1) :-
    write('| X ').
print_item(-1) :-
    write('| O ').
print_item(0) :-
    write('|   ').