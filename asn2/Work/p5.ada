procedure TEST_ADA is

I : INTEGER;

procedure p(X : INTEGER) is
   I,J : INTEGER;

   begin
   I := 1;
   J := 10;
   while I <= J loop
    I := I + 1;
    X := I / 20;
    end loop;
  end P;

begin
P(I);
end;
